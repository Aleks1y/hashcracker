package com.example.manager;

import com.example.manager.dto.HashCrackRequest;
import com.example.manager.dto.HashCrackStatusResponse;
import com.example.manager.entity.RequestStatusEnum;
import com.example.manager.entity.TaskInfo;
import com.example.manager.entity.UnsentTask;
import com.example.manager.repo.TaskInfoRepository;
import com.example.manager.repo.UnsentTaskRepository;
import generated.CrackHashManagerRequest;
import generated.CrackHashWorkerResponse;
import generated.ObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class HashCrackManagerService {
    private static final List<String> ALPHABET = List.of("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","0","1","2","3","4","5","6","7","8","9", "");
    private final int WORKERS_COUNT = 4;

    private final RabbitTemplate template;
    private final TaskInfoRepository taskInfoRepository;
    private final UnsentTaskRepository unsentTaskRepository;
    public String processRequest(HashCrackRequest request) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setRequestTime(LocalDateTime.now());
        taskInfoRepository.save(taskInfo);
        try {
            submitTask(taskInfo.getId(), request.getHash(), request.getMaxLength());
        } catch (AmqpException e){
            unsentTaskRepository.save(new UnsentTask(taskInfo.getId(), request.getHash(), request.getMaxLength()));
        }
        return taskInfo.getId();
    }

    @Scheduled(fixedDelay = 10000)
    private void checkResponses(){
        taskInfoRepository.findByRequestStatusAndRequestTimeBefore(RequestStatusEnum.IN_PROGRESS,
                LocalDateTime.now().minusSeconds(600)).forEach((taskInfo) -> {
            taskInfo.setRequestStatus(RequestStatusEnum.ERROR);
            taskInfoRepository.save(taskInfo);
        });
        try {
            unsentTaskRepository.findAll().forEach(unsentTask -> {
                submitTask(unsentTask.getId(), unsentTask.getHash(), unsentTask.getMaxLength());
                unsentTaskRepository.delete(unsentTask);
            });
        } catch (AmqpException ignored){
        }
    }

    private void submitTask(String requestId, String hash, int maxLength) throws AmqpException{
        ObjectFactory factory = new ObjectFactory();

        CrackHashManagerRequest.Alphabet alphabet = factory.createCrackHashManagerRequestAlphabet();
        alphabet.getSymbols().addAll(ALPHABET);

        CrackHashManagerRequest request = factory.createCrackHashManagerRequest();

        request.setRequestId(requestId);
        request.setHash(hash);
        request.setMaxLength(maxLength);
        request.setAlphabet(alphabet);
        request.setPartCount(WORKERS_COUNT);
        for (int i = 0; i < WORKERS_COUNT; i++) {
            request.setPartNumber(i);
            template.convertAndSend("to_worker", request);
        }
    }

    public HashCrackStatusResponse getStatus(String requestId) {
        TaskInfo taskInfo = taskInfoRepository.findById(requestId).orElse(null);
        if (taskInfo == null){
            return new HashCrackStatusResponse(RequestStatusEnum.ERROR.name(), null);
        }
        RequestStatusEnum requestStatus = taskInfo.getRequestStatus();
        if (requestStatus == RequestStatusEnum.IN_PROGRESS) {
            return new HashCrackStatusResponse(RequestStatusEnum.IN_PROGRESS.name(), null);
        } else if (requestStatus == RequestStatusEnum.READY) {
            return new HashCrackStatusResponse(RequestStatusEnum.READY.name(), taskInfo.getResults());
        } else {
            return new HashCrackStatusResponse(RequestStatusEnum.ERROR.name(), null);
        }
    }

    public void updateRequestStatus(CrackHashWorkerResponse response) {
        TaskInfo taskInfo = taskInfoRepository.findById(response.getRequestId()).orElse(null);
        if(taskInfo == null){
            return;
        }
        taskInfo.setWorkersAnswered(taskInfo.getWorkersAnswered() + 1);
        if(taskInfo.getWorkersAnswered() == WORKERS_COUNT){
            taskInfo.setRequestStatus(RequestStatusEnum.READY);
        }
        taskInfo.getResults().addAll(response.getAnswers().getWords());
        taskInfoRepository.save(taskInfo);
    }
}

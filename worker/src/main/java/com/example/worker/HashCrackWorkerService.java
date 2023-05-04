package com.example.worker;

import generated.CrackHashManagerRequest;
import generated.CrackHashWorkerResponse;
import generated.ObjectFactory;
import lombok.RequiredArgsConstructor;
import org.paukov.combinatorics3.Generator;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HashCrackWorkerService {
    private final RabbitTemplate template;
    public void processRequest(CrackHashManagerRequest request){

        List<String> foundWords = crackHash(request);
        ObjectFactory objectFactory = new ObjectFactory();
        CrackHashWorkerResponse.Answers answers = objectFactory.createCrackHashWorkerResponseAnswers();
        answers.getWords().addAll(foundWords);
        CrackHashWorkerResponse response = objectFactory.createCrackHashWorkerResponse();
        response.setAnswers(answers);
        response.setPartNumber(request.getPartNumber());
        response.setRequestId(request.getRequestId());

        template.convertAndSend("to_manager", response);
    }

    public static List<String> crackHash(CrackHashManagerRequest request){
        List<String> foundWords = new ArrayList<>();
        long partCount = (long) Math.ceil(Math.pow(request.getAlphabet().getSymbols().size(), request.getMaxLength()) / request.getPartCount());
        var stream =  Generator.permutation(request.getAlphabet().getSymbols())
                .withRepetitions(request.getMaxLength())
                .stream()
                .skip(partCount * request.getPartNumber())
                .limit(partCount);
        stream.forEach(word -> {
            if(request.getHash().equals(calculateMd5(String.join("", word)))){
                foundWords.add(String.join("", word));
            }
        });
        return foundWords;
    }
    public static String calculateMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

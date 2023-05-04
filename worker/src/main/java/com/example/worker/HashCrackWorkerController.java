package com.example.worker;

import com.example.worker.HashCrackWorkerService;
import com.rabbitmq.client.Channel;
import generated.CrackHashManagerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/internal/api/worker/hash/crack")
@RequiredArgsConstructor
public class HashCrackWorkerController {

    private final HashCrackWorkerService hashCrackWorkerService;
    @RabbitListener(queues = "to_worker_queue", ackMode = "MANUAL")
    public void worker1(CrackHashManagerRequest request, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        hashCrackWorkerService.processRequest(request);
        channel.basicAck(tag, false);
    }
}

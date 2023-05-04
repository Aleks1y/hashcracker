package com.example.manager;

import com.example.manager.HashCrackManagerService;
import com.example.manager.dto.HashCrackRequest;
import com.example.manager.dto.HashCrackRequestResponse;
import com.example.manager.dto.HashCrackStatusResponse;
import generated.CrackHashWorkerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class HashCrackManagerController {

    private final HashCrackManagerService hashCrackManagerService;

    @PostMapping("/api/hash/crack")
    public ResponseEntity<?> crackHash(@RequestBody HashCrackRequest request) {
        String requestId = hashCrackManagerService.processRequest(request);
        HashCrackRequestResponse response = new HashCrackRequestResponse(requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/hash/status")
    public ResponseEntity<HashCrackStatusResponse> getStatus(@RequestParam String requestId) {
        HashCrackStatusResponse hashCrackStatusResponse = hashCrackManagerService.getStatus(requestId);
        return ResponseEntity.ok(hashCrackStatusResponse);
    }

    @RabbitListener(queues = "to_manager_queue")
    public void worker1(CrackHashWorkerResponse response) {
        hashCrackManagerService.updateRequestStatus(response);
    }
}

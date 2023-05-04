package com.example.manager.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class HashCrackStatusResponse {
    private String status;
    private Set<String> data;

    public HashCrackStatusResponse(String status, Set<String> result) {
        this.status = status;
        if (result != null) {
            this.data = result;
        }
    }

}
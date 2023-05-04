package com.example.manager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HashCrackRequest {
    private String hash;
    private int maxLength;
}

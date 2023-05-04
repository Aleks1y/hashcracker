package com.example.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@AllArgsConstructor
@Document("unsent_tasks")
public class UnsentTask {
    @Id
    private String id;
    private String hash;
    private Integer maxLength;
}

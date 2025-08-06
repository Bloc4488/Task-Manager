package com.example.task_manager.dto;

import com.example.task_manager.entity.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private String userEmail;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.example.task_manager.dto;

import com.example.task_manager.entity.Status;
import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private Status status;
}

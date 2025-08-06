package com.example.task_manager.dto;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String userEmail;
}

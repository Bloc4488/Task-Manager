package com.example.task_manager.controller;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.entity.Status;
import com.example.task_manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public TaskResponse create(@RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);
    }

    @GetMapping()
    public List<TaskResponse> getAll(@RequestParam(required = false)Status status) {
        return taskService.getTasks(status);
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) throws AccessDeniedException {
        return taskService.getTaskById(id);
    }

    @GetMapping("/filter")
    public List<TaskResponse> filterTasks(@RequestParam(required = false) Status status,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) LocalDateTime createdBefore) {
        return taskService.filterTask(status, categoryId, createdBefore);
    }

    @GetMapping("/paged")
    public Page<TaskResponse> getTasksPaged(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort sortBy = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return taskService.getTasksPaged(pageable);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @RequestBody TaskRequest taskRequest) throws AccessDeniedException {
        return taskService.updateTask(id, taskRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws AccessDeniedException {
        taskService.deleteTask(id);
    }
}

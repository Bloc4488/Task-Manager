package com.example.task_manager.service;

import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.TaskResponse;
import com.example.task_manager.entity.Category;
import com.example.task_manager.entity.Status;
import com.example.task_manager.entity.Task;
import com.example.task_manager.entity.User;
import com.example.task_manager.repository.CategoryRepository;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TaskResponse createTask(TaskRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .category(category)
                .build();

        taskRepository.save(task);

        return mapToResponse(task);
    }

    public List<TaskResponse> getTasks(Status status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Task> tasks = (status != null)
                ? taskRepository.findAllByUserIdAndStatus(user.getId(), status)
                : taskRepository.findAllByUserId(user.getId());

        return tasks.stream().map(this::mapToResponse).toList();
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id).orElseThrow();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);

        return mapToResponse(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setTitle(task.getTitle());
        taskResponse.setDescription(task.getDescription());
        taskResponse.setStatus(task.getStatus());
        taskResponse.setUserEmail(task.getUser().getEmail());
        return taskResponse;
    }
}

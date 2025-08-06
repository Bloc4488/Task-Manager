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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
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
        User user = getCurrentUser();
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
        User  user = getCurrentUser();

        List<Task> tasks = (status != null)
                ? taskRepository.findAllByUserIdAndStatus(user.getId(), status)
                : taskRepository.findAllByUserId(user.getId());

        return tasks.stream().map(this::mapToResponse).toList();
    }

    public TaskResponse getTaskById(Long id) throws AccessDeniedException {
        Task task = taskRepository.findById(id).orElseThrow();
        checkTaskOwnership(task);
        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) throws AccessDeniedException {
        Task task = taskRepository.findById(id).orElseThrow();
        checkTaskOwnership(task);
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        task.setCategory(category);

        taskRepository.save(task);

        return mapToResponse(task);
    }

    public void deleteTask(Long id) throws AccessDeniedException {
        Task task = taskRepository.findById(id).orElseThrow();
        checkTaskOwnership(task);
        taskRepository.deleteById(id);
    }

    public List<TaskResponse> filterTask(Status status, Long categoryId, LocalDateTime createdBefore) {
        User user = getCurrentUser();

        return taskRepository.findAll().stream()
                .filter(task -> task.getUser().equals(user))
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> categoryId == null || task.getCategory().getId().equals(categoryId))
                .filter(task -> createdBefore == null || task.getCreatedAt().isBefore(createdBefore))
                .map(this::mapToResponse)
                .toList();
    }

    public Page<TaskResponse> getTasksPaged(Pageable pageable) {
        User user = getCurrentUser();

        return taskRepository.findAllByUserId(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .userEmail(task.getUser().getEmail())
                .categoryName(task.getCategory().getName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private void checkTaskOwnership(Task task) throws AccessDeniedException {
        if (!task.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new AccessDeniedException("You are not allowed to modify this task");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}

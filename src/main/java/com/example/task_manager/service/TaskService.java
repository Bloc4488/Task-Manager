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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return taskRepository.findAll().stream()
                .filter(task -> task.getUser().equals(user))
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> categoryId == null || task.getCategory().getId().equals(categoryId))
                .filter(task -> createdBefore == null || task.getCreatedAt().isBefore(createdBefore))
                .map(this::mapToResponse)
                .toList();
    }

    public Page<TaskResponse> getTasksPaged(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return taskRepository.findAllByUserId(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setTitle(task.getTitle());
        taskResponse.setDescription(task.getDescription());
        taskResponse.setStatus(task.getStatus());
        taskResponse.setCreatedAt(task.getCreatedAt());
        taskResponse.setUpdatedAt(task.getUpdatedAt());
        taskResponse.setCategoryName(task.getCategory().getName());
        taskResponse.setUserEmail(task.getUser().getEmail());
        return taskResponse;
    }

    private void checkTaskOwnership(Task task) throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!task.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not allowed to modify this task");
        }
    }
}

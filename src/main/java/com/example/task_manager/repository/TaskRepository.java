package com.example.task_manager.repository;

import com.example.task_manager.entity.Status;
import com.example.task_manager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserId(Long userId);
    List<Task> findAllByUserIdAndStatus(Long userId, Status status);
}

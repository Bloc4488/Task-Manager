package com.example.task_manager.repository;

import com.example.task_manager.entity.Category;
import com.example.task_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByUser(User user);
}

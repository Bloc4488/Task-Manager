package com.example.task_manager.service;

import com.example.task_manager.dto.CategoryRequest;
import com.example.task_manager.dto.CategoryResponse;
import com.example.task_manager.entity.Category;
import com.example.task_manager.entity.User;
import com.example.task_manager.repository.CategoryRepository;
import com.example.task_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .user(user)
                .build();

        categoryRepository.save(category);

        return mapToResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Category> categories = categoryRepository.findAllByUser(user);
        return categories.stream().map(this::mapToResponse).toList();
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id).orElseThrow();

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setDescription(category.getDescription());
        categoryResponse.setUserEmail(category.getUser().getEmail());
        return categoryResponse;
    }
}

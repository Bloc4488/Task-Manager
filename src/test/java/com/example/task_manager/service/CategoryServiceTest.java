package com.example.task_manager.service;

import com.example.task_manager.dto.CategoryRequest;
import com.example.task_manager.dto.CategoryResponse;
import com.example.task_manager.entity.Category;
import com.example.task_manager.entity.User;
import com.example.task_manager.repository.CategoryRepository;
import com.example.task_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("john@example.com")
                .build();
        category = Category.builder()
                .name("Work")
                .description("Work tasks")
                .user(user)
                .build();
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createCategory_ShouldReturnCategoryResponse_WhenValidRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Work");
        request.setDescription("Work tasks");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Work", response.getName());
        assertEquals("Work tasks", response.getDescription());
        assertEquals("john@example.com", response.getUserEmail());

        verify(userRepository).findByEmail("john@example.com");
        verify(categoryRepository).save(argThat(cat ->
                cat.getName().equals("Work") &&
                        cat.getDescription().equals("Work tasks") &&
                        cat.getUser().equals(user)));
    }

    @Test
    void createCategory_ShouldThrowException_WhenUserNotFound() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Work");
        request.setDescription("Work tasks");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> categoryService.createCategory(request));
        verify(userRepository).findByEmail("john@example.com");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories_WhenUserExists() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findAllByUser(user)).thenReturn(List.of(category));

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        CategoryResponse response = responses.get(0);
        assertEquals("Work", response.getName());
        assertEquals("Work tasks", response.getDescription());
        assertEquals("john@example.com", response.getUserEmail());

        verify(userRepository).findByEmail("john@example.com");
        verify(categoryRepository).findAllByUser(user);
    }

    @Test
    void getAllCategories_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> categoryService.getAllCategories());
        verify(categoryRepository, never()).findAllByUser(any());
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategoryResponse_WhenCategoryExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Work");
        request.setDescription("Updated description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryResponse response = categoryService.updateCategory(1L, request);
        assertNotNull(response);
        assertEquals("Updated Work", response.getName());
        assertEquals("Updated description", response.getDescription());
        assertEquals("john@example.com", response.getUserEmail());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(argThat(cat ->
                cat.getName().equals("Updated Work") &&
                cat.getDescription().equals("Updated description") &&
                cat.getUser().equals(user)));
    }

    @Test
    void updateCategory_ShouldThrowException_WhenCategoryNotFound() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Work");
        request.setDescription("Updated description");
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> categoryService.updateCategory(1L, request));
        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_ShouldCallDelete_WhenCategoryExists() {
        doNothing().when(categoryRepository).deleteById(1L);
        categoryService.deleteCategory(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenCategoryNotFound() {
        doThrow(new NoSuchElementException()).when(categoryRepository).deleteById(1L);
        assertThrows(NoSuchElementException.class,() -> categoryService.deleteCategory(1L));
        verify(categoryRepository).deleteById(1L);
    }
}

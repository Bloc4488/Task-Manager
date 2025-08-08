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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private TaskService taskService;

    private User user;
    private Category category;
    private Task task;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .build();
        category = Category.builder()
                .id(1L)
                .name("Work")
                .description("Work Tasks")
                .user(user)
                .build();
        task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Task Description")
                .category(category)
                .user(user)
                .status(Status.TODO)
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTask_ShouldReturnTaskResponse_WhenValidRequest() throws AccessDeniedException {
        TaskRequest request = new TaskRequest();
        request.setTitle("Task Title");
        request.setDescription("Task Description");
        request.setStatus(Status.TODO);
        request.setCategoryId(1L);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Task Title", response.getTitle());
        assertEquals("Task Description", response.getDescription());
        assertEquals(Status.TODO, response.getStatus());
        assertEquals("john@example.com", response.getUserEmail());
        assertEquals("Work", response.getCategoryName());

        verify(userRepository).findByEmail("john@example.com");
        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(argThat(t ->
                t.getTitle().equals("Task Title") &&
                t.getDescription().equals("Task Description") &&
                t.getStatus().equals(Status.TODO) &&
                t.getUser().equals(user) &&
                t.getCategory().equals(category)));
    }

    @Test
    void createTask_ShouldThrowAccessDeniedException_WhenCategoryNotOwned() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Task Title");
        request.setDescription("Task Description");
        request.setStatus(Status.TODO);
        request.setCategoryId(1L);

        User otherUser = User.builder().id(2L).email("other@example.com").build();
        Category otherCategory = Category.builder().id(1L).name("Other").user(otherUser).build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(otherCategory));

        assertThrows(AccessDeniedException.class, () -> taskService.createTask(request));
        verify(userRepository).findByEmail("john@example.com");
        verify(categoryRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_ShouldThrowIllegalArgumentException_WhenCategoryNotFound() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Task Title");
        request.setDescription("Task Description");
        request.setStatus(Status.TODO);
        request.setCategoryId(1L);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(request));
        verify(userRepository).findByEmail("john@example.com");
        verify(categoryRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getTasks_ShouldReturnTaskList_WhenStatusIsNull() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserId(1L)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getTasks(null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        TaskResponse response = responses.get(0);
        assertEquals("Task", response.getTitle());
        assertEquals("Task Description", response.getDescription());
        assertEquals("Work", response.getCategoryName());

        verify(userRepository).findByEmail("john@example.com");
        verify(taskRepository).findAllByUserId(1L);
    }

    @Test
    void getTasks_ShouldReturnFilteredList_WhenStatusIsProvided() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserIdAndStatus(1L, Status.TODO)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getTasks(Status.TODO);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(Status.TODO, responses.get(0).getStatus());

        verify(userRepository).findByEmail("john@example.com");
        verify(taskRepository).findAllByUserIdAndStatus(1L, Status.TODO);
    }

    @Test
    void getTaskById_ShouldReturnTaskResponse_WhenTaskExistsAndOwned() throws AccessDeniedException {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse response  = taskService.getTaskById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Task", response.getTitle());
        assertEquals("Task Description", response.getDescription());
        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_ShouldThrowAccessDeniedException_WhenTaskNotOwned() {
        User otherUser = User.builder().id(2L).email("other@example.com").build();
        Task otherTask = Task.builder().id(1L).title("Task Title").description("Task Description").user(otherUser).build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(otherTask));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository).findById(1L);
    }

    @Test
    void updateTask_ShouldReturnUpdatedTaskResponse_WhenTaskExistsAndOwned() throws AccessDeniedException {
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Task Description Updated");
        request.setStatus(Status.IN_PROGRESS);
        request.setCategoryId(1L);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTask(1L, request);

        assertNotNull(response);
        assertEquals("Updated Task", response.getTitle());
        assertEquals("Task Description Updated", response.getDescription());
        assertEquals(Status.IN_PROGRESS, response.getStatus());

        verify(taskRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(argThat(t ->
                t.getTitle().equals("Updated Task") &&
                t.getDescription().equals("Task Description Updated") &&
                t.getStatus() == Status.IN_PROGRESS &&
                t.getCategory().equals(category)));
    }
}

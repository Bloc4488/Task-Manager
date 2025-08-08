package com.example.task_manager.service;

import com.example.task_manager.dto.AuthRequest;
import com.example.task_manager.dto.AuthResponse;
import com.example.task_manager.dto.RegisterRequest;
import com.example.task_manager.entity.Role;
import com.example.task_manager.entity.User;
import com.example.task_manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnToken_WhenValidRequest() {
        RegisterRequest registerRequest = new RegisterRequest("John", "Doe", "john@example.com", "password");
        String encodedPassword = "encodedPassword";
        String generatedToken = "token";
        User savedUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn(generatedToken);

        AuthResponse authResponse = authService.register(registerRequest);

        assertEquals(generatedToken, authResponse.getToken());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("john@example.com") &&
                user.getFirstName().equals("John") &&
                user.getLastName().equals("Doe") &&
                user.getPassword().equals(encodedPassword) &&
                user.getRole() == Role.USER
        ));
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("John", "Doe", "john@example.com", "password");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository).findByEmail("john@example.com");
        verifyNoInteractions(passwordEncoder, jwtService);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ShouldReturnToken() {
        AuthRequest request = new AuthRequest("john@example.com", "password");
        String encodedPassword = "encodedPassword";
        String generatedToken = "token";
        User user = User.builder()
                .email("john@example.com")
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(generatedToken);

        AuthResponse authResponse = authService.authenticate(request);

        assertEquals(generatedToken, authResponse.getToken());
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password", encodedPassword);
        verify(jwtService).generateToken(user);
    }

    @Test
    void authenticate_ShouldThrowException_WhenInvalidCredentials() {
        AuthRequest request = new AuthRequest("john@example.com", "wrongPassword");
        User user = User.builder()
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.authenticate(request));
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        AuthRequest request = new AuthRequest("john@example.com", "password");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.authenticate(request));
        verify(userRepository).findByEmail("john@example.com");
        verifyNoInteractions(passwordEncoder, jwtService);
    }
}

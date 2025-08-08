package com.example.task_manager.service;

import com.example.task_manager.entity.Role;
import com.example.task_manager.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    private JwtService jwtService;
    private String secretKey;
    private final long expiration = 86400000;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        jwtService = new JwtService();
        secretKey = "test-secret-key-12345678901234567890123456789012";
        Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtService, secretKey);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        User user = User.builder()
                .email("john@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        var claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("john@example.com", claims.getSubject());
        assertEquals(Role.USER.name(), claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
        assertTrue(claims.getExpiration().before(new Date(System.currentTimeMillis() + expiration + 1000)));
    }

    @Test
    void extractUsername_ShouldReturnEmail_WhenTokenIsValid() {
        User user = User.builder()
                .email("john@example.com")
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        assertEquals("john@example.com", username);
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.value";
        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValidAndMatchesUser() {
        User user = User.builder()
                .email("john@example.com")
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(user);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("john@example.com");
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        User user = User.builder()
                .email("john@example.com")
                .role(Role.USER)
                .build();
        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date(System.currentTimeMillis() - expiration - 1000))
                .setExpiration(new Date(System.currentTimeMillis() - 2000))
                .compact();
        UserDetails userDetails = mock(UserDetails.class);

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatchUser() {
        User user = User.builder()
                .email("john@example.com")
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(user);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("wrong@example.com");

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.value";
        UserDetails userDetails = mock(UserDetails.class);
        boolean isValid = jwtService.isTokenValid(invalidToken, userDetails);
        assertFalse(isValid);
    }
}

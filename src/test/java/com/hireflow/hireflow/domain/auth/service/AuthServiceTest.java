package com.hireflow.hireflow.domain.auth.service;

import com.hireflow.hireflow.domain.auth.dto.LoginRequest;
import com.hireflow.hireflow.domain.auth.dto.LoginResponse;
import com.hireflow.hireflow.domain.auth.dto.SignupRequest;
import com.hireflow.hireflow.domain.user.ResumeParseStatus;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    @InjectMocks
    AuthService authService;

    @Test
    void 회원가입_성공() {
        // Given
        SignupRequest request = new SignupRequest("test@test.com", "password123", "홍길동", "Java,Spring");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        // When
        authService.signup(request);

        // Then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed_password");
        assertThat(saved.getResumeParseStatus()).isEqualTo(ResumeParseStatus.NONE);
    }

    @Test
    void 회원가입_실패_이메일_중복() {
        // Given
        SignupRequest request = new SignupRequest("test@test.com", "password123", "홍길동", "Java,Spring");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void 로그인_성공() {
        // Given
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed_password")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("test@test.com")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("test@test.com")).thenReturn("refresh-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(valueOps).set("refresh:test@test.com", "refresh-token", Duration.ofDays(7));
    }

    @Test
    void 로그인_실패_이메일_없음() {
        // Given
        LoginRequest request = new LoginRequest("none@test.com", "password123");

        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verifyNoInteractions(passwordEncoder,jwtTokenProvider,redisTemplate);

    }

    @Test
    void 로그인_실패_비밀번호_불일치() {
        // Given
        LoginRequest request = new LoginRequest("test@test.com", "wrong");
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed_password")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        verifyNoInteractions(jwtTokenProvider,redisTemplate);
    }
}

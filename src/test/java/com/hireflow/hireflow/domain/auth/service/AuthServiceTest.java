package com.hireflow.hireflow.domain.auth.service;

import com.hireflow.hireflow.domain.auth.dto.SignupRequest;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

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
        verify(userRepository).save(any(User.class));
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
    }
}

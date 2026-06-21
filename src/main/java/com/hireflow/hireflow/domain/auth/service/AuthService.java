package com.hireflow.hireflow.domain.auth.service;

import com.hireflow.hireflow.domain.auth.dto.LoginRequest;
import com.hireflow.hireflow.domain.auth.dto.LoginResponse;
import com.hireflow.hireflow.domain.auth.dto.SignupRequest;
import com.hireflow.hireflow.domain.user.ResumeParseStatus;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.security.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .techStack(request.getTechStack())
                .resumeParseStatus(ResumeParseStatus.NONE)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // Redis에 refreshToken에 저장 (key: "refresh: {email}", TTL: 7일"
        redisTemplate.opsForValue().set(
                "refresh:" + user.getEmail(),
                refreshToken,
                Duration.ofDays(7)
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    public String refresh(String refreshToken) {
        // 1. 토큰 유효성 검증
        String email;
        try {
            email = jwtTokenProvider.getEmailFromToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("refreshToken이 만료되었습니다. 다시 로그인해주세요.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 refreshToken입니다.");
        }

        // 2. Redis에 저장된 토큰과 일치하는지 확인
        String stored = redisTemplate.opsForValue().get("refresh:" + email);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new IllegalArgumentException("이미 로그아웃된 토큰입니다.");
        }

        // 3. 새 accessToken 발급
        return jwtTokenProvider.generateAccessToken(email);
    }

    public void logout(String refreshToken) {
        String email;
        try {
            email = jwtTokenProvider.getEmailFromToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 파싱 실패해도 로그아웃은 성공 처리
            return;
        }

        redisTemplate.delete("refresh:" + email);
    }
}

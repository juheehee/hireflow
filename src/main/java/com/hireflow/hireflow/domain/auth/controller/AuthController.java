package com.hireflow.hireflow.domain.auth.controller;

import com.hireflow.hireflow.domain.auth.dto.*;
import com.hireflow.hireflow.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호로 회원가입. 이메일 중복 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 중복 또는 입력값 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(null));
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인. accessToken 30분, refreshToken 7일 유효.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 — accessToken, refreshToken 반환"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(response));
    }

    @Operation(summary = "토큰 재발급", description = "refreshToken으로 새 accessToken 발급.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "accessToken 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 refreshToken")
    })
    @PostMapping("/refresh")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<RefreshResponse>> refresh(
            @RequestBody RefreshRequest request) {
        String newAccessToken = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(
                new RefreshResponse(newAccessToken)));
    }

    @Operation(summary = "로그아웃", description = "refreshToken을 Redis에서 삭제. 이후 해당 refreshToken으로 재발급 불가.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<Void>> logout(
            @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(null));
    }
}
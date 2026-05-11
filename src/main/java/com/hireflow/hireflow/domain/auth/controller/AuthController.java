package com.hireflow.hireflow.domain.auth.controller;

import com.hireflow.hireflow.domain.auth.dto.LoginRequest;
import com.hireflow.hireflow.domain.auth.dto.LoginResponse;
import com.hireflow.hireflow.domain.auth.dto.SignupRequest;
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
}
package com.sesac2ndproject.attendancemanagementsystem.domain.auth.controller;


import com.sesac2ndproject.attendancemanagementsystem.domain.auth.dto.AuthRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.auth.dto.AuthResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.auth.service.AuthService;
import com.sesac2ndproject.attendancemanagementsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Auth", description = "로그인 및 회원가입 인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Login", description = "user에게 id/pw 를 입력받아 JWT 토큰을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        ApiResponse<AuthResponse> response = ApiResponse.success(authService.login(request));

        // JSON 형태로 반환하기
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String bearerToken
    ) {
        // 1. 헤더에서 순수 토큰 값 추출 ("Bearer " 제거)
        String accessToken = resolveToken(bearerToken);

        // 2. 서비스 로그아웃 로직 호출
        authService.logout(accessToken);

        // 3. 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.success());
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

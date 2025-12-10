package com.sesac2ndproject.attendancemanagementsystem.domain.auth.service;

import com.sesac2ndproject.attendancemanagementsystem.domain.auth.dto.AuthRequest;
import com.sesac2ndproject.attendancemanagementsystem.domain.auth.dto.AuthResponse;
import com.sesac2ndproject.attendancemanagementsystem.domain.refreshtoken.entity.RefreshToken;
import com.sesac2ndproject.attendancemanagementsystem.domain.refreshtoken.repository.RefreshTokenRepository;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.entity.Member;
import com.sesac2ndproject.attendancemanagementsystem.domain.member.repository.MemberRepository;
import com.sesac2ndproject.attendancemanagementsystem.global.error.CustomException;
import com.sesac2ndproject.attendancemanagementsystem.global.error.ErrorCode;
import com.sesac2ndproject.attendancemanagementsystem.global.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인 기능
    @Transactional
    public AuthResponse login(AuthRequest request) {

        Member member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getLoginId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        refreshTokenRepository.findById(member.getLoginId())
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(member.getLoginId(), refreshToken))
                );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(member.getId())
                .loginId(member.getLoginId())
                .name(member.getName())
                .role(member.getRole())
                .build();
    }


    @Transactional
    public void deleteToken(String loginId) {
        // 해당 회원의 리프레시 토큰이 존재하면 삭제
        if (refreshTokenRepository.existsById(loginId)) {
            refreshTokenRepository.deleteById(loginId);
        }
    }

    @Transactional
    public void logout(String accessToken) {
        // 1. Access Token 검증 및 Login ID 추출
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        String loginId = jwtTokenProvider.getLoginId(accessToken);

        // 2. RefreshTokenService를 통해 토큰 삭제 (로그아웃 처리)
        deleteToken(loginId);
    }

}

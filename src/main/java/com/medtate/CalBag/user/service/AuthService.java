package com.medtate.CalBag.user.service;

import com.medtate.CalBag.auth.jwt.JwtProvider;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.user.domain.RefreshToken;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.dto.AuthResponse;
import com.medtate.CalBag.user.dto.LoginRequest;
import com.medtate.CalBag.user.dto.SignupRequest;
import com.medtate.CalBag.user.repository.RefreshTokenRepository;
import com.medtate.CalBag.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        // 서명·만료·타입 검증 → DB에서 삭제(1건 삭제돼야 유효). 삭제 자체가 "한 번만 사용" 보장
        if (!jwtProvider.validateRefreshToken(refreshToken)
                || refreshTokenRepository.deleteByToken(refreshToken) == 0) {
            throw new BusinessException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.getReferenceById(jwtProvider.getUserId(refreshToken).intValue());
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    private AuthResponse issueTokens(User user) {
        refreshTokenRepository.deleteExpiredByUserId(user.getId(), LocalDateTime.now());

        String accessToken = jwtProvider.generateAccessToken(user.getId().longValue());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId().longValue());

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(jwtProvider.getExpiresAt(refreshToken))
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
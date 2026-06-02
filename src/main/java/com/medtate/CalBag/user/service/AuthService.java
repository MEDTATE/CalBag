package com.medtate.CalBag.user.service;

import com.medtate.CalBag.auth.jwt.JwtProvider;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.dto.AuthResponse;
import com.medtate.CalBag.user.dto.LoginRequest;
import com.medtate.CalBag.user.dto.SignupRequest;
import com.medtate.CalBag.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        return AuthResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(user.getId().longValue()))
                .refreshToken(jwtProvider.generateRefreshToken(user.getId().longValue()))
                .build();
    }
}
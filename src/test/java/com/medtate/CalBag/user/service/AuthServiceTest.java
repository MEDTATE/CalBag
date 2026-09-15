package com.medtate.CalBag.user.service;

import com.medtate.CalBag.auth.jwt.JwtProvider;
import com.medtate.CalBag.global.exception.BusinessException;
import com.medtate.CalBag.user.domain.RefreshToken;
import com.medtate.CalBag.user.domain.User;
import com.medtate.CalBag.user.dto.AuthResponse;
import com.medtate.CalBag.user.repository.RefreshTokenRepository;
import com.medtate.CalBag.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String refreshToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(
                User.builder()
                        .email("authtest@test.com")
                        .passwordHash("hashedpassword")
                        .name("테스트유저")
                        .build()
        );

        refreshToken = jwtProvider.generateRefreshToken(user.getId().longValue());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(refreshToken)
                        .expiresAt(jwtProvider.getExpiresAt(refreshToken))
                        .build()
        );
    }

    @Test
    @DisplayName("리프레시하면 새 토큰이 발급되고, 이전 리프레시 토큰은 다시 쓸 수 없다")
    void refresh_rotatesToken() {
        AuthResponse response = authService.refresh(refreshToken);

        assertThat(response.getRefreshToken()).isNotEqualTo(refreshToken);
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");

        // 새로 받은 토큰은 사용 가능
        assertThat(authService.refresh(response.getRefreshToken()).getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그아웃한 리프레시 토큰은 사용할 수 없다")
    void logout_invalidatesToken() {
        authService.logout(refreshToken);

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }
}
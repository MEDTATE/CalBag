package com.medtate.CalBag.auth.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            "test-secret-key-must-be-at-least-32-bytes-long", 1800000, 604800000);

    @Test
    @DisplayName("리프레시 토큰은 액세스 토큰으로 사용할 수 없다")
    void refreshTokenCannotBeUsedAsAccessToken() {
        String refreshToken = jwtProvider.generateRefreshToken(1L);

        assertThat(jwtProvider.validateAccessToken(refreshToken)).isFalse();
        assertThat(jwtProvider.validateRefreshToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("액세스 토큰은 리프레시 토큰으로 사용할 수 없다")
    void accessTokenCannotBeUsedAsRefreshToken() {
        String accessToken = jwtProvider.generateAccessToken(1L);

        assertThat(jwtProvider.validateRefreshToken(accessToken)).isFalse();
        assertThat(jwtProvider.validateAccessToken(accessToken)).isTrue();
    }
}
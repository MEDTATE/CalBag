package com.medtate.CalBag.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessTokenExpiration, ACCESS);
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshTokenExpiration, REFRESH);
    }

    private String generateToken(Long userId, long expiration, String type) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE, type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, REFRESH);
    }

    private boolean validateToken(String token, String type) {
        try {
            return type.equals(getClaims(token).get(TOKEN_TYPE, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

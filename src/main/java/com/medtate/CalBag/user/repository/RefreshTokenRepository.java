package com.medtate.CalBag.user.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medtate.CalBag.user.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.token = :token")
    int deleteByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId AND r.expiresAt < :now")
    void deleteExpiredByUserId(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
}

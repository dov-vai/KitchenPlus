package com.kitchenplus.kitchenplus.data.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kitchenplus.kitchenplus.data.models.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByToken(String token);
    Optional<Session> findByTokenAndExpiresAtAfter(String token, LocalDateTime expiresAt);
}
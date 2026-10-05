package com.user.register.repository;

import com.user.register.entity.UserSession;
import com.user.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUser(User user);
    void deleteByUser(User user);

    Optional<UserSession> findFirstByToken(String token);
    default Optional<UserSession> findByToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return findFirstByToken(token);
    }

    Optional<UserSession> findFirstByAccessToken(String token);
    default Optional<UserSession> findByAccessToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return findFirstByAccessToken(token);
    }

    Optional<UserSession> findFirstByRefreshToken(String token);
    default Optional<UserSession> findByRefreshToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return findFirstByRefreshToken(token);
    }

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from UserSession s where s.refreshToken = :token")
    Optional<UserSession> findForUpdateByRefreshToken(@org.springframework.data.repository.query.Param("token") String token);
}

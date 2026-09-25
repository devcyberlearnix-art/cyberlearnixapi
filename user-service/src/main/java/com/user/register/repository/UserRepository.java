package com.user.register.repository;

import com.user.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findFirstByEmail(String email);
    default Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return findFirstByEmail(email);
    }

    Optional<User> findFirstByMobile(String mobile);
    default Optional<User> findByMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) return Optional.empty();
        return findFirstByMobile(mobile);
    }

    Optional<User> findFirstByMobileHash(String mobileHash);
    default Optional<User> findByMobileHash(String mobileHash) {
        if (mobileHash == null || mobileHash.isBlank()) return Optional.empty();
        return findFirstByMobileHash(mobileHash);
    }

    Optional<User> findFirstByResetToken(String resetToken);
    default Optional<User> findByResetToken(String resetToken) {
        if (resetToken == null || resetToken.isBlank()) return Optional.empty();
        return findFirstByResetToken(resetToken);
    }

    // Count registrations by email or mobile in the last 1 hour (for rate limiting)
    @Query("SELECT COUNT(u) FROM User u WHERE (u.email = :email OR u.mobile = :mobile) AND u.createdAt >= :after")
    long countByEmailOrMobileAndCreatedAtAfter(@Param("email") String email,
                                               @Param("mobile") String mobile,
                                               @Param("after") LocalDateTime after);

    long countByRole(User.Role role);
    long countByStatus(User.Status status);
    long countByCreatedAtAfter(LocalDateTime after);
    java.util.List<User> findByRole(User.Role role);
}
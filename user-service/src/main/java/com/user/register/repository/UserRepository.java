package com.user.register.repository;

import com.user.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
    Optional<User> findForUpdateById(@org.springframework.data.repository.query.Param("id") UUID id);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByMobileHash(String mobileHash);
    // ✅ Required for reset password

    // Count registrations by email or mobile in the last 1 hour (for rate limiting)
    @Query("SELECT COUNT(u) FROM User u WHERE (u.email = :email OR u.mobile = :mobile) AND u.createdAt >= :after")
    long countByEmailOrMobileAndCreatedAtAfter(@Param("email") String email,
                                               @Param("mobile") String mobile,
                                               @Param("after") LocalDateTime after);
    Optional<User> findByResetToken(String resetToken);

    long countByRole(User.Role role);
    long countByStatus(User.Status status);
    long countByCreatedAtAfter(LocalDateTime after);
    java.util.List<User> findByRole(User.Role role);
}

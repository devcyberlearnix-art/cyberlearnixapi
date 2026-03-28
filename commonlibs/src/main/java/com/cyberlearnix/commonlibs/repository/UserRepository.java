package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByAlternatePhoneNumber(String alternatePhoneNumber);
}

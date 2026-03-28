package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.CartConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartConfigurationRepository extends JpaRepository<CartConfiguration, Long> {

    /**
     * Find configuration by key
     */
    Optional<CartConfiguration> findByConfigKey(String configKey);

    /**
     * Check if configuration exists
     */
    boolean existsByConfigKey(String configKey);
}

package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

    /**
     * Find store by name
     */
    Optional<StoreEntity> findByStoreName(String storeName);

    /**
     * Find stores by active status
     */
    List<StoreEntity> findByIsActive(Boolean isActive);

    /**
     * Find active stores
     */
    @Query("SELECT s FROM StoreEntity s WHERE s.isActive = true")
    List<StoreEntity> findActiveStores();

    /**
     * Find stores within radius
     */
    @Query(value = "SELECT * FROM stores s WHERE " +
           "s.is_active = true AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(s.latitude)))) <= :radiusKm", 
           nativeQuery = true)
    List<StoreEntity> findStoresWithinRadius(@Param("latitude") Double latitude,
                                           @Param("longitude") Double longitude,
                                           @Param("radiusKm") Double radiusKm);

    /**
     * Check if store exists by name
     */
    boolean existsByStoreName(String storeName);

    /**
     * Find by storeId (explicit Optional wrapper used in merchantservice).
     */
    Optional<StoreEntity> findByStoreId(Long storeId);

    /**
     * Resolve store for a given merchant registration.
     */
    Optional<StoreEntity> findByMerchantRegistrationId(UUID merchantRegistrationId);

    /**
     * Additional helpers referenced by merchantservice controllers/services
     */
    Optional<StoreEntity> findFirstByEmailIgnoreCase(String email);
    Optional<StoreEntity> findFirstByPhone(String phone);

    /**
     * Find active stores by locality (city or state)
     */
    @Query(value = "SELECT * FROM stores s " +
            "WHERE s.is_active = true " +
            "AND (LOWER(s.city) = LOWER(:locality) OR LOWER(s.state) = LOWER(:locality)) " +
            "ORDER BY s.premium_status DESC, s.rating DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<StoreEntity> findActiveStoresByLocality(@Param("locality") String locality, @Param("limit") int limit);
}

package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.Inventory;
import com.cyberlearnix.commonlibs.entity.InventoryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, InventoryKey> {

    /**
     * Find inventory by store and product
     */
    @Query("SELECT i FROM Inventory i WHERE i.id.storeId = :storeId AND i.id.productId = :productId")
    Optional<Inventory> findByStoreStoreIdAndProductProductId(@Param("storeId") Long storeId, 
                                                               @Param("productId") Long productId);

    /**
     * Lightweight stock lookup to avoid loading full Inventory/Product entities.
     */
    @Query("SELECT i.stock FROM Inventory i WHERE i.id.storeId = :storeId AND i.id.productId = :productId")
    Optional<Integer> findStockByStoreIdAndProductId(@Param("storeId") Long storeId,
                                               @Param("productId") Long productId);

    /**
     * Atomic stock decrement; returns number of rows updated (0 => insufficient stock / missing row).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Inventory i SET i.stock = i.stock - :quantity " +
           "WHERE i.id.storeId = :storeId AND i.id.productId = :productId AND i.stock >= :quantity")
    int decrementStockIfAvailable(@Param("storeId") Long storeId,
                             @Param("productId") Long productId,
                             @Param("quantity") Integer quantity);

    /**
     * Find all inventory by store
     */
    @Query("SELECT i FROM Inventory i WHERE i.id.storeId = :storeId")
    List<Inventory> findByStoreStoreId(@Param("storeId") Long storeId);

    /**
     * Find all inventory by product
     */
    @Query("SELECT i FROM Inventory i WHERE i.id.productId = :productId")
    List<Inventory> findByProductProductId(@Param("productId") Long productId);

    /**
     * Find inventory with stock above threshold
     */
    @Query("SELECT i FROM Inventory i WHERE i.id.storeId = :storeId AND i.stock > :threshold")
    List<Inventory> findByStoreStoreIdAndStockGreaterThan(@Param("storeId") Long storeId, 
                                                          @Param("threshold") Integer threshold);

    /**
     * Find low stock items
     */
    @Query("SELECT i FROM Inventory i WHERE i.stock <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();

    /**
     * Find available inventory (in stock)
     */
    @Query("SELECT i FROM Inventory i WHERE i.stock > 0 AND i.status = 'AVAILABLE'")
    List<Inventory> findAvailableInventory();

    /**
     * Check if inventory exists for store and product
     */
    @Query("SELECT COUNT(i) > 0 FROM Inventory i WHERE i.id.storeId = :storeId AND i.id.productId = :productId")
    boolean existsByStoreStoreIdAndProductProductId(@Param("storeId") Long storeId, 
                                                    @Param("productId") Long productId);

    @Query("SELECT DISTINCT i.id.productId FROM Inventory i " +
           "WHERE i.id.storeId = :storeId AND i.inStock = TRUE AND i.stock > 0")
    List<Long> findInStockProductIdsByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT DISTINCT i.id.productId FROM Inventory i " +
           "WHERE i.id.storeId = :storeId AND (i.inStock = FALSE OR i.stock <= 0)")
    List<Long> findOutOfStockProductIdsByStoreId(@Param("storeId") Long storeId);
}

package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByTxnId(String txnId);

    Optional<PaymentTransaction> findFirstByCartIdAndStatusOrderByCreatedAtDesc(Long cartId, PaymentTransaction.TxnStatus status);

    boolean existsByCartIdAndStatus(Long cartId, PaymentTransaction.TxnStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'INITIATED' AND pt.expiresAt < :now")
    List<PaymentTransaction> findExpiredTransactions(@Param("now") LocalDateTime now);

    List<PaymentTransaction> findByCartIdOrderByCreatedAtDesc(Long cartId);
}

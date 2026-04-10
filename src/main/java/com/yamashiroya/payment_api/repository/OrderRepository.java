package com.yamashiroya.payment_api.repository;

import com.yamashiroya.payment_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentSessionId(String paymentSessionId);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.createdAt >= :from and o.createdAt < :to")
    long sumTotalAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByStatusAndConfirmedAtBetween(String status, LocalDateTime from, LocalDateTime to);

    @Query("select coalesce(sum(o.finalAmount), 0) from Order o where o.status = :status and o.confirmedAt >= :from and o.confirmedAt < :to")
    long sumFinalAmountByStatusBetween(
            @Param("status") String status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

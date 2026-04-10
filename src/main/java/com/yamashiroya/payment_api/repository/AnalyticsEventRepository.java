package com.yamashiroya.payment_api.repository;

import com.yamashiroya.payment_api.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByEventTypeAndCreatedAtBetween(String eventType, LocalDateTime from, LocalDateTime to);

    boolean existsByEventTypeAndSessionId(String eventType, String sessionId);

    @Query("select e.channel, count(e) from AnalyticsEvent e where e.eventType = :eventType and e.createdAt >= :from and e.createdAt < :to group by e.channel")
    List<Object[]> countByChannelGrouped(@Param("eventType") String eventType, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

package com.yamashiroya.payment_api.service;

import com.yamashiroya.payment_api.dto.AdminStatsResponse;
import com.yamashiroya.payment_api.dto.FunnelStats;
import com.yamashiroya.payment_api.dto.PeriodStats;
import com.yamashiroya.payment_api.repository.AnalyticsEventRepository;
import com.yamashiroya.payment_api.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminStatsService {

    private static final String EVENT_SESSION = "SESSION";
    private static final String EVENT_CART_ADD = "CART_ADD";
    private static final String EVENT_CHECKOUT_START = "CHECKOUT_START";
    private static final String EVENT_CHECKOUT_COMPLETE = "CHECKOUT_COMPLETE";

    private final OrderRepository orderRepository;
    private final AnalyticsEventRepository analyticsEventRepository;

    public AdminStatsService(OrderRepository orderRepository, AnalyticsEventRepository analyticsEventRepository) {
        this.orderRepository = orderRepository;
        this.analyticsEventRepository = analyticsEventRepository;
    }

    public AdminStatsResponse getMonthlyStats(YearMonth month) {
        YearMonth currentMonth = month == null ? YearMonth.now() : month;
        YearMonth prevMonth = currentMonth.minusMonths(1);

        LocalDateTime currentFrom = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime currentTo = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        LocalDateTime prevFrom = prevMonth.atDay(1).atStartOfDay();
        LocalDateTime prevTo = prevMonth.plusMonths(1).atDay(1).atStartOfDay();

        AdminStatsResponse response = new AdminStatsResponse();
        response.setCurrent(buildPeriodStats(currentFrom, currentTo));
        response.setPrevious(buildPeriodStats(prevFrom, prevTo));
        return response;
    }

    public AdminStatsResponse getStats(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate endExclusive = (to == null ? start.plusMonths(1) : to);

        LocalDateTime currentFrom = start.atStartOfDay();
        LocalDateTime currentTo = endExclusive.atStartOfDay();

        long days = java.time.temporal.ChronoUnit.DAYS.between(currentFrom, currentTo);
        LocalDateTime prevTo = currentFrom;
        LocalDateTime prevFrom = currentFrom.minusDays(days);

        AdminStatsResponse response = new AdminStatsResponse();
        response.setCurrent(buildPeriodStats(currentFrom, currentTo));
        response.setPrevious(buildPeriodStats(prevFrom, prevTo));
        return response;
    }

    private PeriodStats buildPeriodStats(LocalDateTime from, LocalDateTime to) {
        long totalRevenue = orderRepository.sumTotalAmountBetween(from, to);
        long totalOrders = orderRepository.countByCreatedAtBetween(from, to);
        long totalSessions = analyticsEventRepository.countByEventTypeAndCreatedAtBetween(EVENT_SESSION, from, to);

        System.out.println("[admin-stats] from=" + from + " to=" + to + " revenue=" + totalRevenue + " orders=" + totalOrders + " sessions=" + totalSessions);

        FunnelStats funnel = new FunnelStats();
        funnel.setCartAdds(analyticsEventRepository.countByEventTypeAndCreatedAtBetween(EVENT_CART_ADD, from, to));
        funnel.setCheckoutStarts(analyticsEventRepository.countByEventTypeAndCreatedAtBetween(EVENT_CHECKOUT_START, from, to));
        funnel.setCheckoutCompletes(analyticsEventRepository.countByEventTypeAndCreatedAtBetween(EVENT_CHECKOUT_COMPLETE, from, to));

        Map<String, Long> channels = new HashMap<>();
        List<Object[]> rows = analyticsEventRepository.countByChannelGrouped(EVENT_SESSION, from, to);
        for (Object[] r : rows) {
            String channel = r[0] == null ? "unknown" : String.valueOf(r[0]);
            Long count = r[1] == null ? 0L : ((Number) r[1]).longValue();
            channels.put(channel, count);
        }

        PeriodStats stats = new PeriodStats();
        stats.setFrom(from.toString());
        stats.setTo(to.toString());
        stats.setTotalRevenue(totalRevenue);
        stats.setTotalOrders(totalOrders);
        stats.setTotalSessions(totalSessions);
        stats.setPurchaseRate(totalSessions <= 0 ? 0.0 : (double) totalOrders / (double) totalSessions);
        stats.setFunnel(funnel);
        stats.setChannels(channels);
        return stats;
    }
}

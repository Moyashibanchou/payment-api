package com.yamashiroya.payment_api.service;

import com.yamashiroya.payment_api.entity.AnalyticsEvent;
import com.yamashiroya.payment_api.repository.AnalyticsEventRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsEventService {

    private final AnalyticsEventRepository analyticsEventRepository;

    public AnalyticsEventService(AnalyticsEventRepository analyticsEventRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
    }

    public void record(String eventType, String channel, String sessionId) {
        if (eventType == null || eventType.isBlank()) {
            return;
        }

        if (sessionId != null && !sessionId.isBlank()) {
            if (analyticsEventRepository.existsByEventTypeAndSessionId(eventType, sessionId)) {
                return;
            }
        }

        AnalyticsEvent e = new AnalyticsEvent();
        e.setEventType(eventType);
        e.setChannel(channel);
        e.setSessionId(sessionId);
        analyticsEventRepository.save(e);
    }
}

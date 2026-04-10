package com.yamashiroya.payment_api.controller;

import com.yamashiroya.payment_api.dto.AnalyticsEventRequest;
import com.yamashiroya.payment_api.service.AnalyticsEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsEventService analyticsEventService;

    public AnalyticsController(AnalyticsEventService analyticsEventService) {
        this.analyticsEventService = analyticsEventService;
    }

    @PostMapping("/event")
    public ResponseEntity<?> recordEvent(@RequestBody AnalyticsEventRequest request) {
        analyticsEventService.record(request.getEventType(), request.getChannel(), request.getSessionId());
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        return ResponseEntity.ok(result);
    }
}

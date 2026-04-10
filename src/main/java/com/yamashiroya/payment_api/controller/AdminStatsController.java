package com.yamashiroya.payment_api.controller;

import com.yamashiroya.payment_api.dto.AdminStatsResponse;
import com.yamashiroya.payment_api.service.AdminStatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(
        origins = {
                "https://yamashiroya.vercel.app",
                "http://localhost:5173"
        },
        allowCredentials = "true"
)
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping
    public ResponseEntity<AdminStatsResponse> getStats(
            HttpServletRequest request,
            @RequestParam(value = "month", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        System.out.println("★API Request Received: " + request.getRequestURI());
        if (month != null) {
            return ResponseEntity.ok(adminStatsService.getMonthlyStats(month));
        }
        return ResponseEntity.ok(adminStatsService.getStats(from, to));
    }
}

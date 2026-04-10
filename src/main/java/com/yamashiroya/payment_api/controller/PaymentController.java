package com.yamashiroya.payment_api.controller;

import com.yamashiroya.payment_api.dto.PaymentRequest;
import com.yamashiroya.payment_api.entity.Order;
import com.yamashiroya.payment_api.repository.OrderRepository;
import com.yamashiroya.payment_api.service.AnalyticsEventService;
import com.yamashiroya.payment_api.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final String EVENT_CHECKOUT_START = "CHECKOUT_START";
    private static final String EVENT_CHECKOUT_COMPLETE = "CHECKOUT_COMPLETE";

    @Value("${komoju.secret-key}")
    private String secretKey;

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    private final RestTemplate restTemplate;

    private final EmailService emailService;

    private final AnalyticsEventService analyticsEventService;

    private final OrderRepository orderRepository;

    public PaymentController(
            RestTemplate restTemplate,
            EmailService emailService,
            AnalyticsEventService analyticsEventService,
            OrderRepository orderRepository
    ) {
        this.restTemplate = restTemplate;
        this.emailService = emailService;
        this.analyticsEventService = analyticsEventService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/create-session")
    public ResponseEntity<?> createSession(HttpServletRequest request, @RequestBody PaymentRequest paymentRequest) {
        try {
            System.out.println("★API Request Received: " + request.getRequestURI());
            String url = "https://komoju.com/api/v1/sessions";

            // KOMOJU APIへのリクエストボディ作成
            Map<String, Object> body = new HashMap<>();
            body.put("amount", paymentRequest.getAmount());
            body.put("currency", "JPY");
            String base = (frontendUrl == null) ? "http://localhost:5173" : frontendUrl.trim();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            body.put("return_url", base + "/success");
            body.put("cancel_url", base + "/checkout");
            body.put("payment_types", Collections.singletonList(paymentRequest.getPaymentMethod()));

            // Basic認証の設定
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // KOMOJU API呼び出し
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String sessionUrl = (String) response.getBody().get("session_url");

                String paymentSessionId = null;
                Object idObj = response.getBody().get("id");
                if (idObj != null) {
                    paymentSessionId = String.valueOf(idObj);
                }
                if (paymentSessionId == null || paymentSessionId.isBlank()) {
                    Object sessionIdObj = response.getBody().get("session_id");
                    if (sessionIdObj != null) {
                        paymentSessionId = String.valueOf(sessionIdObj);
                    }
                }
                analyticsEventService.record(EVENT_CHECKOUT_START, null, paymentSessionId);

                Map<String, String> result = new HashMap<>();
                result.put("checkoutUrl", sessionUrl);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("KOMOJU API error");
            }

        } catch (Exception e) {
            e.printStackTrace(); // サーバーログにスタックトレースを出力
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/verify-session")
    public ResponseEntity<?> verifySession(
            HttpServletRequest request,
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "items", required = false) String items,
            @RequestParam(value = "totalAmount", required = false) Integer totalAmount
    ) {
        try {
            System.out.println("★API Request Received: " + request.getRequestURI());
            String url = "https://komoju.com/api/v1/sessions/" + sessionId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            boolean verified = false;
            Integer resolvedAmount = totalAmount;
            String resolvedCurrency = "JPY";
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("[verify-session] komoju response keys=" + response.getBody().keySet());
                Object statusObj = response.getBody().get("status");
                String status = statusObj == null ? "" : String.valueOf(statusObj);
                System.out.println("[verify-session] komoju status=" + status);
                verified = status.equalsIgnoreCase("completed")
                        || status.equalsIgnoreCase("paid")
                        || status.equalsIgnoreCase("succeeded")
                        || status.equalsIgnoreCase("authorized");

                if (resolvedAmount == null || resolvedAmount <= 0) {
                    Object amountObj = response.getBody().get("amount");
                    System.out.println("[verify-session] komoju amount(raw)=" + amountObj);
                    if (amountObj instanceof Number) {
                        resolvedAmount = ((Number) amountObj).intValue();
                    } else if (amountObj != null) {
                        try {
                            resolvedAmount = Integer.parseInt(String.valueOf(amountObj));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                Object currencyObj = response.getBody().get("currency");
                System.out.println("[verify-session] komoju currency(raw)=" + currencyObj);
                if (currencyObj != null && !String.valueOf(currencyObj).isBlank()) {
                    resolvedCurrency = String.valueOf(currencyObj);
                }
            }

            if (resolvedAmount == null) {
                resolvedAmount = 0;
            }

            System.out.println("[verify-session] verified=" + verified + ", sessionId=" + sessionId + ", resolvedAmount=" + resolvedAmount + ", resolvedCurrency=" + resolvedCurrency);

            Map<String, Object> result = new HashMap<>();
            result.put("verified", verified);

            if (verified && email != null && !email.isBlank()) {
                String resolvedOrderNo = (orderNo == null || orderNo.isBlank()) ? "(unknown)" : orderNo;
                emailService.sendOrderConfirmationEmail(email, resolvedOrderNo, items, totalAmount);
            }

            if (verified) {
                analyticsEventService.record(EVENT_CHECKOUT_COMPLETE, null, sessionId);

                if (orderRepository.findByPaymentSessionId(sessionId).isEmpty()) {
                    Order order = new Order();
                    order.setPaymentSessionId(sessionId);
                    order.setOrderNo(orderNo);
                    order.setEmail(email);
                    order.setCurrency(resolvedCurrency);
                    order.setTotalAmount(resolvedAmount);
                    orderRepository.save(order);
                    System.out.println("[verify-session] order saved: sessionId=" + sessionId + ", amount=" + resolvedAmount);
                } else {
                    orderRepository.findByPaymentSessionId(sessionId).ifPresent(existing -> {
                        Integer existingAmount = existing.getTotalAmount();
                        if ((existingAmount == null || existingAmount <= 0) && resolvedAmount != null && resolvedAmount > 0) {
                            existing.setTotalAmount(resolvedAmount);
                            existing.setCurrency(resolvedCurrency);
                            orderRepository.save(existing);
                            System.out.println("[verify-session] order updated: sessionId=" + sessionId + ", amount=" + resolvedAmount);
                        } else {
                            System.out.println("[verify-session] order already exists: sessionId=" + sessionId);
                        }
                    });
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("verified", false);
            return ResponseEntity.ok(result);
        }
    }
}

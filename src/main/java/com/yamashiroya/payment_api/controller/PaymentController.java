package com.yamashiroya.payment_api.controller;

import com.yamashiroya.payment_api.dto.PaymentRequest;
import com.yamashiroya.payment_api.service.EmailService;
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

    @Value("${komoju.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    private final EmailService emailService;

    public PaymentController(RestTemplate restTemplate, EmailService emailService) {
        this.restTemplate = restTemplate;
        this.emailService = emailService;
    }

    @PostMapping("/create-session")
    public ResponseEntity<?> createSession(@RequestBody PaymentRequest paymentRequest) {
        try {
            String url = "https://komoju.com/api/v1/sessions";

            // KOMOJU APIへのリクエストボディ作成
            Map<String, Object> body = new HashMap<>();
            body.put("amount", paymentRequest.getAmount());
            body.put("currency", "JPY");
            body.put("return_url", "http://localhost:5173/success");
            body.put("cancel_url", "http://localhost:5173/checkout");
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
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "items", required = false) String items,
            @RequestParam(value = "totalAmount", required = false) Integer totalAmount
    ) {
        try {
            String url = "https://komoju.com/api/v1/sessions/" + sessionId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            boolean verified = false;
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object statusObj = response.getBody().get("status");
                String status = statusObj == null ? "" : String.valueOf(statusObj);
                verified = status.equalsIgnoreCase("completed")
                        || status.equalsIgnoreCase("paid")
                        || status.equalsIgnoreCase("succeeded")
                        || status.equalsIgnoreCase("authorized");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("verified", verified);

            if (verified && email != null && !email.isBlank()) {
                String resolvedOrderNo = (orderNo == null || orderNo.isBlank()) ? "(unknown)" : orderNo;
                emailService.sendOrderConfirmationEmail(email, resolvedOrderNo, items, totalAmount);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("verified", false);
            return ResponseEntity.ok(result);
        }
    }
}

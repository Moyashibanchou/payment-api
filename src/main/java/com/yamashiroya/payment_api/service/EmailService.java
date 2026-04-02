package com.yamashiroya.payment_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${spring.mail.username:}")
    private String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendEmail(String to, String subject, String body) {
        if (mockEnabled) {
            log.info("[MOCK MAIL] To: {}", to);
            log.info("[MOCK MAIL] Subject: {}", subject);
            log.info("[MOCK MAIL] Body:\n{}", body);
            return;
        }

        if (mailSender == null) {
            throw new IllegalStateException("JavaMailSender is not configured. Configure spring.mail.* properties or enable app.mail.mock-enabled.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendOrderConfirmationEmail(String to, String orderNo, String items, Integer totalAmount) {
        String subject = "【ご注文確認】注文番号 " + orderNo;

        String safeItems = items == null ? "" : items;
        String total = totalAmount == null ? "" : String.valueOf(totalAmount);

        String body = "ご注文ありがとうございます。\n\n" +
                "注文番号: " + orderNo + "\n" +
                "購入商品: " + safeItems + "\n" +
                "合計金額: " + total + " 円\n\n" +
                "またのご利用をお待ちしております。\n";

        sendEmail(to, subject, body);
    }
}

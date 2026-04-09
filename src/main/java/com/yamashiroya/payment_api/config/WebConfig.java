package com.yamashiroya.payment_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = buildAllowedOrigins();
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(buildAllowedOrigins());
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    private List<String> buildAllowedOrigins() {
        List<String> origins = new ArrayList<>();
        origins.add("http://localhost:5173");
        origins.add("http://localhost:5174");
        origins.add("https://yamashiroya.vercel.app");
        origins.addAll(normalizeOrigins(frontendUrl));
        return origins;
    }

    private List<String> normalizeOrigins(String raw) {
        if (raw == null) {
            return List.of();
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        String[] parts = trimmed.split(",");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            if (p == null) {
                continue;
            }
            String o = p.trim();
            if (o.isEmpty()) {
                continue;
            }
            if (o.endsWith("/")) {
                o = o.substring(0, o.length() - 1);
            }
            if (!o.isEmpty()) {
                result.add(o);
            }
        }
        return result;
    }
}

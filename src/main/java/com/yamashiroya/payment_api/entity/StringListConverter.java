package com.yamashiroya.payment_api.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return toCsv(attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return fromCsv(dbData);
    }

    public static String toCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        List<String> cleaned = new ArrayList<>();
        for (String v : values) {
            if (v == null) {
                continue;
            }
            String t = v.trim();
            if (!t.isEmpty()) {
                cleaned.add(t);
            }
        }

        return String.join(",", cleaned);
    }

    public static List<String> fromCsv(String csv) {
        if (csv == null) {
            return Collections.emptyList();
        }

        String trimmed = csv.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = trimmed.split(",");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            if (p == null) {
                continue;
            }
            String t = p.trim();
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }
}

package com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache;

import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class SearchKeyGenerator {

    private static final String CACHE_KEY_PREFIX = "customers:list:";
    private static final String CUSTOMER_DETAIL_PREFIX = "customers:id:";

    public String generateSearchKey(int page, int size, CustomerType type) {
        String typeStr = type != null ? type.name() : "ALL";
        String searchParams = String.format("page:%d:size:%d:type:%s", page, size, typeStr);
        String hash = generateHash(searchParams);
        return CACHE_KEY_PREFIX + hash;
    }

    public String generateCustomerDetailKey(String customerId) {
        return CUSTOMER_DETAIL_PREFIX + customerId;
    }

    public String getCustomerListPattern() {
        return CACHE_KEY_PREFIX + "*";
    }

    public String getCustomerDetailPattern() {
        return CUSTOMER_DETAIL_PREFIX + "*";
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

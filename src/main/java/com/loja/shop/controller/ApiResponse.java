package com.loja.shop.controller;

import org.springframework.http.ResponseEntity;
import java.util.Map;

final class ApiResponse {
    private ApiResponse() {}
    static ResponseEntity<Map<String, Object>> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}

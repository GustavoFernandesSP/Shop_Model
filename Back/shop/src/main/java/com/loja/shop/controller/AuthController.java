package com.loja.shop.controller;

import com.loja.shop.dto.Requests;
import com.loja.shop.model.AppUser;
import com.loja.shop.repository.UserRepository;
import com.loja.shop.security.JwtAuthFilter;
import com.loja.shop.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Requests.Register req) {
        if (req.email() == null || req.email().isBlank() || req.password() == null || req.password().length() < 6) {
            return ApiResponse.error(400, "Informe e-mail e uma senha com no mínimo 6 caracteres");
        }
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            return ApiResponse.error(409, "Este e-mail já está cadastrado");
        }
        AppUser u = new AppUser();
        u.setName(req.username() != null && !req.username().isBlank() ? req.username() : req.name());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        users.save(u);
        return loggedIn(u);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Requests.Login req) {
        if (req.email() == null || req.password() == null) {
            return ApiResponse.error(400, "Informe e-mail e senha");
        }
        AppUser u = users.findByEmail(req.email().trim().toLowerCase()).orElse(null);
        if (u == null || !encoder.matches(req.password(), u.getPasswordHash())) {
            return ApiResponse.error(401, "E-mail ou senha inválidos");
        }
        return loggedIn(u);
    }

    // o front chama /logout com GET
    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .body(Map.of("message", "Logout realizado"));
    }

    private ResponseEntity<Map<String, Object>> loggedIn(AppUser u) {
        Map<String, Object> user = new HashMap<>();
        user.put("_id", u.getId());   // o front pode ler _id ou id
        user.put("id", u.getId());
        user.put("name", u.getName());
        user.put("username", u.getName());
        user.put("email", u.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie(jwt.create(u.getId()), Duration.ofDays(7)).toString())
                .body(Map.of("user", user));
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(JwtAuthFilter.COOKIE, value)
                .httpOnly(true)
                .secure(false)        // em produção com HTTPS: true
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}

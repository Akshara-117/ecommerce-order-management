package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.*;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthService {
    private final CustomerRepository customers;
    private final CustomerCredentialRepository credentials;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtEncoder jwtEncoder;

    public AuthService(CustomerRepository customers, CustomerCredentialRepository credentials,
                       @Value("${JWT_SECRET:}") String jwtSecret) {
        this.customers = customers;
        this.credentials = credentials;
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("Set JWT_SECRET to a random secret of at least 32 characters");
        }
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(
                new SecretKeySpec(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256")));
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (customers.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        Customer customer = customers.save(new Customer(request.name(), email));
        credentials.save(new CustomerCredential(customer, passwordEncoder.encode(request.password())));
        return Map.of("customerId", customer.id, "email", customer.email);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Customer customer = customers.findByEmailIgnoreCase(email)
                .orElseThrow(this::invalidCredentials);
        CustomerCredential credential = credentials.findById(customer.id)
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), credential.passwordHash)) {
            throw invalidCredentials();
        }
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ecommerce-order-management")
                .subject(customer.id.toString())
                .issuedAt(now).expiresAt(now.plus(1, ChronoUnit.HOURS))
                .claim("email", customer.email).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return Map.of("accessToken", token, "tokenType", "Bearer", "expiresIn", 3600);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}

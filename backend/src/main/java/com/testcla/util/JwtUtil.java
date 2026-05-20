package com.testcla.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "TestClaSecretKey2026ForJWTHMAC256Bits!!";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7天

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("phone must not be null or empty");
        }
        return Jwts.builder()
                .subject(phone)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public String getPhoneFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token must not be null or empty");
        }
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

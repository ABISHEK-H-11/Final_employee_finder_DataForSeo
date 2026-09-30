package com.employeeFinderByDataForSEO.service;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET_KEY;
    private final long EXPIRATION_TIME ; // 1 hour

    public JwtService(@Value("${jwt.secret}") String SECRET_KEY,
                      @Value("${jwt.expiration}") long EXPIRATION_TIME) {
        this.SECRET_KEY = SECRET_KEY;
        this.EXPIRATION_TIME = EXPIRATION_TIME;
    }


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String email) {

//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }
}
package com.marcptr.jwt.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.TokenType;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    private Key key;

    @PostConstruct
    public void init() {

        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret cannot be empty");
        }

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 256 bits (32 bytes) for HS256");
        }

        if (jwtExpiration <= 0 || refreshExpiration <= 0) {
            throw new IllegalStateException(
                    "JWT expiration values must be greater than zero");
        }

        if (issuer == null || issuer.isBlank()
                || audience == null || audience.isBlank()) {
            throw new IllegalStateException(
                    "JWT issuer and audience must be configured");
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRefreshJti(String token) {
        return extractClaim(token,
                claims -> claims.get("refresh_jti", String.class));
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {

        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(
                userDetails,
                refreshExpiration,
                TokenType.REFRESH,
                null);
    }

    public String generateAccessToken(
            UserDetails userDetails,
            String refreshToken) {

        String refreshJti = extractJti(refreshToken);

        return generateToken(
                userDetails,
                jwtExpiration,
                TokenType.ACCESS,
                refreshJti);
    }

    private String generateToken(
            UserDetails userDetails,
            long expirationTime,
            TokenType type,
            String refreshJti) {

        User user = (User) userDetails;

        JwtBuilder builder = Jwts.builder()
                .claim("role", user.getRole().name())
                .claim("type", type.name())
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("version", "v1")
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setSubject(userDetails.getUsername())
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expirationTime));

        if (type == TokenType.ACCESS && refreshJti != null) {
            builder.claim("refresh_jti", refreshJti);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails,
            TokenType expectedType) {

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            String typeStr = claims.get("type", String.class);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (username == null || typeStr == null
                    || jti == null || expiration == null) {
                return false;
            }

            TokenType actualType = TokenType.valueOf(typeStr);

            return username.equals(userDetails.getUsername())
                    && actualType == expectedType
                    && expiration.after(new Date())
                    && userDetails.isEnabled()
                    && userDetails.isAccountNonLocked()
                    && userDetails.isAccountNonExpired()
                    && userDetails.isCredentialsNonExpired();

        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT cannot be empty");
        }

        Jws<Claims> parsedToken = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token);

        if (!SignatureAlgorithm.HS256.getValue()
                .equals(parsedToken.getHeader().getAlgorithm())) {
            throw new UnsupportedJwtException(
                    "Unexpected JWT signing algorithm");
        }

        return parsedToken.getBody();
    }
}
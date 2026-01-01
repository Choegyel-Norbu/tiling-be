package com.tilingroofing.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations.
 * Handles token generation, validation, and claim extraction.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration:3600000}") long jwtExpiration
    ) {
        // Validate secret key length (minimum 256 bits = 32 characters)
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 characters (256 bits) long. " +
                    "Current length: " + (jwtSecret != null ? jwtSecret.length() : 0)
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = jwtExpiration;
        
        log.info("JWT Token Provider initialized - Expiration: {} ms ({} minutes)", 
                jwtExpiration, jwtExpiration / 60000);
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param userId   User's unique identifier (as String)
     * @param email    User's email address
     * @param name     User's display name
     * @return JWT token string
     */
    public String generateToken(String userId, String email, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", name);
        return createToken(claims, userId);
    }

    /**
     * Creates a JWT token with claims and subject.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        log.debug("Creating token - Issued at: {}, Expires at: {}, Expiration duration: {} ms ({} minutes)", 
                now, expiryDate, jwtExpiration, jwtExpiration / 60000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the user ID (subject) from a token.
     */
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extracts the email from a token.
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }

    /**
     * Extracts the name from a token.
     */
    public String getNameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("name", String.class));
    }

    /**
     * Extracts the expiration date from a token.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a token.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a token.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates a token.
     * Checks if token is expired and signature is valid.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            boolean expired = isTokenExpired(claims);
            
            if (expired) {
                Date expiration = claims.getExpiration();
                Date now = new Date();
                long timeUntilExpiry = expiration.getTime() - now.getTime();
                log.warn("Token expired. Expiration: {}, Current: {}, Time until expiry (ms): {}", 
                        expiration, now, timeUntilExpiry);
            }
            
            return !expired;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a token is expired.
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        Date now = new Date();
        boolean expired = expiration.before(now);
        
        if (expired) {
            long timeSinceExpiry = now.getTime() - expiration.getTime();
            log.debug("Token expired {} ms ago", timeSinceExpiry);
        }
        
        return expired;
    }

}


package com.admanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.InputStream;
import java.security.Key;
import java.util.Date;
import java.util.Properties;

/**
 * JWT Token utility for authentication
 */
public class JWTUtil {
    private static String SECRET_KEY;
    private static long EXPIRATION_TIME;

    static {
        try {
            Properties props = new Properties();
            InputStream input = JWTUtil.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            props.load(input);
            
            SECRET_KEY = props.getProperty("jwt.secret");
            EXPIRATION_TIME = Long.parseLong(props.getProperty("jwt.expiration"));
            
            input.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT configuration", e);
        }
    }

    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generate JWT token for user
     */
    public static String generateToken(int userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract user ID from token
     */
    public static int getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extract username from token
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("username", String.class);
    }

    /**
     * Check if token is expired
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

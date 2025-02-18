package com.coda.assignment.routing_service.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Objects;

@Service
@Slf4j
public class JWTAuthorizationService {

    private final SecretKey signingKey;

    JWTAuthorizationService(@Value("${jwt.secret:}") String secretKey) {
        byte[] decodedKey = Decoders.BASE64.decode(secretKey);
        signingKey = Keys.hmacShaKeyFor(decodedKey);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateJWTToken(HttpServletRequest request) {
        String token = resolveToken(request);
        try {
            if (Objects.nonNull(token)) {
                Jws<Claims> jwt = Jwts.parser().verifyWith(signingKey).build().parseClaimsJws(token);
                return Objects.nonNull(jwt.getBody().getSubject());
            }
        } catch (ExpiredJwtException ex) {
            log.error("ExpiredJwtException Token: {}", token);
        } catch (UnsupportedJwtException ex) {
            log.error("UnsupportedJwtException Token: {}", token);
        } catch (MalformedJwtException ex) {
            log.error("MalformedJwtException Token: {}", token);
        } catch (IllegalArgumentException ex) {
            log.error("IllegalArgumentException Token: {}", token);
        }
        return false;
    }
}

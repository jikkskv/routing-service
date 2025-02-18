package com.coda.assignment.routing_service.service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import java.security.Key;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTAuthorizationServiceTest {

    private JWTAuthorizationService jwtAuthorizationService;

    @Mock
    private HttpServletRequest request;

    private Key signingKey;
    private static final String SECRET_KEY = "2CxClM+bOgS8rIRt36caFfyKFjLZW6FRTfG7PDx/ong=";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        jwtAuthorizationService = new JWTAuthorizationService(SECRET_KEY);
        signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        ReflectionTestUtils.setField(jwtAuthorizationService, "signingKey", signingKey);
    }

    private String generateToken(String subject, long expirationMillis) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void testResolveToken_Success() {
        String token = "Bearer testToken123";
        when(request.getHeader("Authorization")).thenReturn(token);

        String resolvedToken = jwtAuthorizationService.resolveToken(request);
        assertEquals("testToken123", resolvedToken);
    }

    @Test
    void testResolveToken_NoTokenProvided() {
        when(request.getHeader("Authorization")).thenReturn(null);

        String resolvedToken = jwtAuthorizationService.resolveToken(request);
        assertNull(resolvedToken);
    }

    @Test
    void testValidateJWTToken_Success() {
        String validToken = "Bearer " + generateToken("user123", 60000);
        when(request.getHeader("Authorization")).thenReturn(validToken);

        boolean isValid = jwtAuthorizationService.validateJWTToken(request);
        assertTrue(isValid);
    }

    @Test
    void testValidateJWTToken_FailedForExpiredToken() {
        String expiredToken = "Bearer " + generateToken("user123", -1000);
        when(request.getHeader("Authorization")).thenReturn(expiredToken);

        boolean isValid = jwtAuthorizationService.validateJWTToken(request);
        assertFalse(isValid);
    }

    @Test
    void testValidateJWTToken_FailedForMalformedToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed.token.here");

        boolean isValid = jwtAuthorizationService.validateJWTToken(request);
        assertFalse(isValid);
    }
}

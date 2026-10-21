package com.bcp.workshop.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final Instant NOW = Instant.parse("2030-05-01T12:00:00.750Z");

    @Test
    void issuesOnlyRequiredClaimsFromControlledClock() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        TokenService service = new TokenService(SECRET, clock);

        TokenService.IssuedToken issued = service.issue("student", "web-client");
        DecodedJWT decoded = service.verify(issued.token());

        assertEquals(Set.of("sub", "client", "iat", "exp"), decoded.getClaims().keySet());
        assertEquals("student", decoded.getSubject());
        assertEquals("web-client", decoded.getClaim("client").asString());
        assertEquals(NOW.getEpochSecond(), decoded.getIssuedAt().toInstant().getEpochSecond());
        assertEquals(decoded.getIssuedAt().getTime() / 1000 + 3600,
                decoded.getExpiresAt().getTime() / 1000);
        assertEquals(3600, issued.expiresIn());
        assertTrue(Math.abs(issued.expiresIn()
                - (decoded.getExpiresAt().toInstant().getEpochSecond() - NOW.getEpochSecond())) <= 1);
    }

    @Test
    void verifiesSignatureAndRejectsModifiedOrExpiredTokens() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        TokenService service = new TokenService(SECRET, clock);
        String token = service.issue("student", "web-client").token();
        String[] segments = token.split("\\.");
        segments[2] = (segments[2].charAt(0) == 'A' ? "B" : "A") + segments[2].substring(1);

        assertTrue(service.isValid(token));
        assertFalse(service.isValid(String.join(".", segments)));

        String expiringToken = JWT.create()
                .withSubject("student")
                .withClaim("client", "web-client")
                .withIssuedAt(Date.from(NOW.minusSeconds(60)))
                .withExpiresAt(Date.from(NOW))
                .sign(Algorithm.HMAC256(SECRET));
        assertFalse(service.isValid(expiringToken));
        assertFalse(new TokenService(SECRET, Clock.fixed(NOW.plusSeconds(1), ZoneOffset.UTC))
                .isValid(expiringToken));
    }

    @Test
    void refusesToCreateServiceWithoutAConfiguredSecret() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () -> new TokenService(null, clock));
        assertThrows(IllegalStateException.class, () -> new TokenService("too-short", clock));
        assertEquals("student", new TokenService(SECRET, clock)
                .verify(new TokenService(SECRET, clock).issue("student", "web-client").token())
                .getSubject());
    }
}
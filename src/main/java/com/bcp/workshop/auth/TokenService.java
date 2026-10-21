package com.bcp.workshop.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class TokenService {
    private static final long TOKEN_TTL_SECONDS = 3600;

    private final String secret;
    private final Clock clock;

    @Inject
    public TokenService(@ConfigProperty(name = "auth.token.secret") String secret) {
        this(secret, Clock.systemUTC());
    }

    TokenService(String secret, Clock clock) {
        validateSecret(secret);
        this.secret = secret;
        this.clock = clock;
    }

    static void validateSecret(String value) {
        if (value == null || value.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("AUTH_TOKEN_SECRET must contain at least 32 UTF-8 bytes");
        }
    }

    public IssuedToken issue(String username, String client) {
        long issuedAt = clock.instant().getEpochSecond();
        long expiresAt = issuedAt + TOKEN_TTL_SECONDS;
        String token = JWT.create()
                .withSubject(username)
                .withClaim("client", client)
                .withIssuedAt(Date.from(Instant.ofEpochSecond(issuedAt)))
                .withExpiresAt(Date.from(Instant.ofEpochSecond(expiresAt)))
                .sign(Algorithm.HMAC256(secret));
        return new IssuedToken(token, expiresAt - issuedAt);
    }

    public DecodedJWT verify(String token) {
        DecodedJWT decoded = JWT.decode(token);
        if (!"HS256".equals(decoded.getAlgorithm())) {
            throw new IllegalArgumentException("Unsupported token algorithm");
        }
        Algorithm.HMAC256(secret).verify(decoded);
        if (decoded.getSubject() == null || decoded.getClaim("client").isNull()
                || decoded.getIssuedAt() == null) {
            throw new IllegalArgumentException("Required token claim is missing");
        }
        Date expiresAt = decoded.getExpiresAt();
        if (expiresAt == null || clock.instant().getEpochSecond() >= expiresAt.toInstant().getEpochSecond()) {
            throw new IllegalArgumentException("Token expired");
        }
        return decoded;
    }

    public boolean isValid(String token) {
        try {
            verify(token);
            return true;
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            return false;
        }
    }

    public record IssuedToken(String token, long expiresIn) {
    }
}
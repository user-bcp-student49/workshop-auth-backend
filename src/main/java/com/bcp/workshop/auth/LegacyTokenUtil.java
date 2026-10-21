package com.bcp.workshop.auth;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@ApplicationScoped
public class LegacyTokenUtil {
    private static final long TOKEN_TTL_SECONDS = 3600;

    @ConfigProperty(name = "auth.token.secret")
    String secret;

    @PostConstruct
    void validateConfiguration() {
        validateSecret(secret);
    }

    static void validateSecret(String value) {
        if (value == null || value.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("AUTH_TOKEN_SECRET must contain at least 32 UTF-8 bytes");
        }
    }

    public IssuedToken create(String username, String role, String client) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + TOKEN_TTL_SECONDS;
        String token = Jwt.claims()
                .subject(username)
                .claim("role", role)
                .claim("client", client)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .jws()
                .algorithm(SignatureAlgorithm.HS256)
                .sign(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return new IssuedToken(token, expiresAt - issuedAt);
    }

    public record IssuedToken(String token, long expiresIn) {
    }
}

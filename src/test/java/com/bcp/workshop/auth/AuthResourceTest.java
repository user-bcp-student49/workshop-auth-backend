package com.bcp.workshop.auth;

import io.quarkus.test.junit.QuarkusTest;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthResourceTest {
    private static final String TEST_SECRET = "test-only-secret-0123456789abcdef0123456789abcdef";

    @Inject
    TokenService tokenService;

    @Test
    void logsInWithWorkshopUserAndReturnsCompatibleSignedToken() throws Exception {
        var response = given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("user.displayName", is("Workshop Student"))
                .body("user.role", is("STUDENT"))
                .body("user", not(hasKey("password")))
                .body("user", not(hasKey("hash")))
                .body("user", not(hasKey("client")))
                .body("expiresIn", is(3600))
                .body("debug", is("authenticated-with-legacy-flow"))
                .body("token", notNullValue());

        String token = response.extract().jsonPath().getString("token");
        long expiresIn = response.extract().jsonPath().getLong("expiresIn");
    DecodedJWT claims = tokenService.verify(token);
    assertEquals("student", claims.getSubject());
    assertEquals("angular-workshop", claims.getClaim("client").asString());
    assertTrue(claims.getIssuedAt().before(claims.getExpiresAt()));
    assertEquals(3600, claims.getExpiresAt().toInstant().getEpochSecond()
        - claims.getIssuedAt().toInstant().getEpochSecond());
    assertEquals(3600, expiresIn);
    assertEquals(Set.of("sub", "client", "iat", "exp"), claims.getClaims().keySet());
    }

    @Test
    void requiresNonBlankClientAndAcceptsArbitraryClient() {
        for (String body : new String[]{
            "{\"username\":\"student\",\"password\":\"Workshop123!\"}",
            "{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"\"}",
            "{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"   \"}"
        }) {
            given()
                .contentType("application/json")
                .body(body)
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Client is required"))
                .body("token", nullValue());
        }

        String token = given()
            .contentType("application/json")
            .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\" other \"}")
            .when().post("/auth/login")
            .then()
            .statusCode(200)
            .extract().jsonPath().getString("token");
        assertEquals(" other ", tokenService.verify(token).getClaim("client").asString());
    }

    @Test
    void rejectsInvalidCredentials() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"wrong\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", is("Invalid user student or password"))
                .body("token", nullValue());
            }

            @Test
            void preservesExistingRequiredFieldErrors() {
            given()
                .contentType("application/json")
                .body("null")
                .when().post("/auth/login")
                .then().statusCode(400).body("message", is("Request is null"));

            given()
                .contentType("application/json")
                .body("{\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then().statusCode(400).body("message", is("Username is required"));

            given()
                .contentType("application/json")
                .body("{\"username\":\"student\"}")
                .when().post("/auth/login")
                .then().statusCode(400).body("message", is("Password is required"));
            }

            @Test
            void doesNotLogTokenSecretOrPasswordsForSuccessfulAndFailedLogin() {
            var rootLogger = LogManager.getLogManager().getLogger("");
            var capturedOutput = new ByteArrayOutputStream();
            var handler = new StreamHandler(capturedOutput, new SimpleFormatter());
            rootLogger.addHandler(handler);
            String token;
            try {
                token = given()
                    .contentType("application/json")
                    .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                    .when().post("/auth/login")
                    .then().statusCode(200).extract().jsonPath().getString("token");

                given()
                    .contentType("application/json")
                    .body("{\"username\":\"student\",\"password\":\"failed-password\",\"client\":\"angular-workshop\"}")
                    .when().post("/auth/login")
                    .then().statusCode(401);
            } finally {
                handler.flush();
                rootLogger.removeHandler(handler);
                handler.close();
            }

            String logs = capturedOutput.toString(StandardCharsets.UTF_8);
            assertFalse(logs.contains(token));
            assertFalse(logs.contains(TEST_SECRET));
            assertFalse(logs.contains("Workshop123!"));
            assertFalse(logs.contains("failed-password"));
    }

    @Test
    void requiresStrongConfiguredSecret() {
        assertThrows(IllegalStateException.class, () -> TokenService.validateSecret(null));
        assertThrows(IllegalStateException.class, () -> TokenService.validateSecret("too-short"));
        assertDoesNotThrow(() -> TokenService.validateSecret(
                "0123456789abcdef0123456789abcdef"));
    }
}

package com.bcp.workshop.auth;

import io.quarkus.test.junit.QuarkusTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthResourceTest {
    @Test
    void logsInWithWorkshopUserAndReturnsCompatibleSignedToken() throws Exception {
        var response = given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("token", notNullValue());

        String token = response.extract().jsonPath().getString("token");
        long expiresIn = response.extract().jsonPath().getLong("expiresIn");
        String[] segments = token.split("\\.");
        assertEquals(3, segments.length);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode header = mapper.readTree(Base64.getUrlDecoder().decode(segments[0]));
        JsonNode claims = mapper.readTree(Base64.getUrlDecoder().decode(segments[1]));
        assertEquals("HS256", header.get("alg").asText());
        assertEquals("student", claims.get("sub").asText());
        assertEquals("STUDENT", claims.get("role").asText());
        assertEquals("angular-workshop", claims.get("client").asText());
        assertTrue(claims.has("iat"));
        assertTrue(claims.has("exp"));
        assertTrue(Math.abs(expiresIn - (claims.get("exp").asLong() - Instant.now().getEpochSecond())) <= 5);
    }

    @Test
    void rejectsMissingOrUnsupportedClient() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then().statusCode(400);

        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"other\"}")
                .when().post("/auth/login")
                .then().statusCode(400);
    }

    @Test
    void rejectsInvalidCredentials() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"wrong\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    void requiresStrongConfiguredSecret() {
        assertThrows(IllegalStateException.class, () -> LegacyTokenUtil.validateSecret(null));
        assertThrows(IllegalStateException.class, () -> LegacyTokenUtil.validateSecret("too-short"));
        assertDoesNotThrow(() -> LegacyTokenUtil.validateSecret(
                "0123456789abcdef0123456789abcdef"));
    }
}

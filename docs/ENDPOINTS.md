# Contrato de endpoints

Este documento describe el comportamiento implementado actualmente. Los consumidores deben conservar el contrato de `POST /auth/login`.

## `POST /auth/login`

Autentica un usuario contra la base SQLite configurada y, si las credenciales son válidas, devuelve un token y datos del usuario.

- **Content-Type de solicitud:** `application/json`
- **Content-Type de respuesta:** `application/json`
- **Cuerpo:**

  ```json
  {
    "username": "string",
    "password": "string",
    "client": "string (opcional)"
  }
  ```

  `client` está presente en el modelo de solicitud, pero el flujo actual no lo utiliza.

- **Respuesta `200 OK`:**

  ```json
  {
    "token": "<token>",
    "expiresIn": 3600,
    "user": {
      "username": "<username>",
      "displayName": "<displayName escapado como HTML>",
      "role": "<role>"
    },
    "debug": "authenticated-with-legacy-flow"
  }
  ```

- **Respuestas de error:**
  - `400 Bad Request`: solicitud nula o `username`/`password` nulo o vacío.
  - `401 Unauthorized`: no se encuentra una coincidencia de credenciales.
  - `500 Internal Server Error`: otra excepción no controlada por la ruta; la respuesta actual incluye el mensaje y el tipo de excepción.

## Flujo actual de login

1. `AuthResource.login` valida que la solicitud, el usuario y la contraseña no sean nulos ni estén vacíos.
2. `LegacyDatabase.findByCredentials` consulta SQLite. La URL usa `AUTH_DB_URL` o el valor predeterminado `jdbc:sqlite:./data/workshop.db`; la inicialización crea la tabla y usuarios de laboratorio si no existen.
3. Si no hay coincidencia, la API responde `401`. Si se encuentra el usuario, `LegacyTokenUtil.create` genera el token y el recurso construye la respuesta `200`.
4. El token contiene usuario, rol, expiración de una hora y secreto; el contenido se codifica en Base64.

## `GET /q/health`

Endpoint de salud provisto por la extensión SmallRye Health de Quarkus.

- **Respuesta:** estado de salud de la aplicación en formato JSON, según la extensión de Quarkus.
- **Content-Type:** `application/json`.

## Configuración relevante actual

- Puerto HTTP: `8080`.
- Host local: `127.0.0.1`; perfil `%prod`: `0.0.0.0`.
- CORS está habilitado con cualquier origen (`/.*/`); métodos y encabezados permitidos están definidos en `application.properties`.

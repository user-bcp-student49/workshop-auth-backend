# Tasks

## 1. Pruebas de caracterización

- [x] 1.1 Añadir una prueba de integración para `POST /auth/login` que caracterice el éxito actual: status, `token`, `expiresIn`, `user` y `debug`.
- [x] 1.2 Caracterizar los campos de `user` y verificar que no incluya contraseña, hash ni `client`.
- [x] 1.3 Caracterizar status y mensajes actuales para request nulo, username/password ausentes y credenciales inválidas.
- [x] 1.4 Registrar que actualmente `client` es opcional; cambiar esa expectativa al implementar el requisito nuevo y no preservar la omisión como comportamiento compatible.
- [x] 1.5 Asegurar que ninguna prueba trate el contenido del token legacy reversible como contrato que deba preservarse.

## 2. Pruebas del servicio de tokens

- [x] 2.1 Añadir pruebas unitarias que verifiquen los claims `sub`, `client`, `iat` y `exp`, con reloj controlable.
- [x] 2.2 Añadir pruebas de firma verificable y de rechazo del token cuando el tiempo actual sea igual o posterior a `exp`.
- [x] 2.3 Probar que `expiresIn` coincide con `exp - now` dentro de un segundo.
- [x] 2.4 Probar que la ausencia de secreto fuera del sandbox impide emitir un token.

## 3. Servicio y configuración

- [x] 3.1 Incorporar `java-jwt` con una versión mantenida y configurar HS256 según `design.md`.
- [x] 3.2 Crear el servicio aislado de emisión y verificación, con claims mínimos y duración de 3600 segundos.
- [x] 3.3 Proveer un reloj controlable para las pruebas del servicio de tokens.
- [x] 3.4 Cargar `AUTH_TOKEN_SECRET` de configuración externa, sin fallback integrado fuera del sandbox.
- [x] 3.5 Documentar la configuración requerida y cualquier secreto exclusivo del sandbox, sin exponer su valor.

## 4. Integración del login

- [x] 4.1 Validar `client` no nulo ni blanco después de las validaciones existentes de username/password, conservando el valor aceptado sin recortarlo ni normalizarlo.
- [x] 4.2 Delegar la emisión del token al servicio nuevo y eliminar la llamada al token legacy y el secreto hardcodeado.
- [x] 4.3 Mantener `POST /auth/login`, los errores existentes y la estructura de respuesta exitosa (`token`, `expiresIn`, `user`, `debug`).
- [x] 4.4 Derivar `iat`, `exp` y `expiresIn` de la misma ventana temporal para cumplir la tolerancia de un segundo.
- [x] 4.5 No añadir fallback ni aceptación dual del formato legacy.

## 5. Pruebas de integración y validación

- [x] 5.1 Probar login válido, claims requeridos y firma verificable.
- [x] 5.2 Probar `client` ausente, vacío o solo espacios: `400 Bad Request` y ningún token emitido.
- [x] 5.3 Probar credenciales inválidas: `401 Unauthorized`, error preservado y ningún token emitido.
- [x] 5.4 Probar que se preservan los campos públicos de `user`, `debug` y los errores documentados de request nulo y username/password requeridos.
- [x] 5.5 Probar rechazo de token vencido y coherencia de `expiresIn` con `exp` dentro de un segundo.
- [x] 5.6 Verificar que los logs de login exitoso y fallido no contienen token, secreto, contraseña ni otros valores sensibles.
- [x] 5.7 Ejecutar las pruebas del proyecto y validar `token-firmado-claims-minimos` con OpenSpec.
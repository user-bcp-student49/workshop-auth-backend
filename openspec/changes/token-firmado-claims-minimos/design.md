# Design: Token firmado con claims mínimos

## Contexto técnico

El checkout actual es una aplicación Java 21 con Quarkus 3.27.4. `POST /auth/login` valida `username` y `password`, obtiene el usuario desde SQLite y arma directamente la respuesta HTTP. En un login exitoso llama a `LegacyTokenUtil`, que genera Base64 de `username|role|expiry|SECRET`; esto no es una firma ni un JWT. El secreto está hardcodeado y el request ya contiene `client`, aunque hoy el endpoint lo ignora. La respuesta incluye `token`, `expiresIn` (3600 segundos), `user` y `debug`.

Maven no declara una biblioteca JWT ni hay un servicio de emisión o validación separado en este checkout. La prueba de integración existente solo comprueba un login exitoso y que el token no sea nulo. La propuesta y la spec del cambio fijan claims y comportamiento; esta decisión técnica concreta una de las alternativas que el Issue #12 deja abierta.

## Goals

- Emitir un JWS compacto verificable con `sub`, `client`, `iat` y `exp`.
- Conservar los 3600 segundos de vigencia declarada y hacer que `expiresIn` concuerde con `exp - now` dentro de un segundo.
- Requerir `client` no nulo ni en blanco antes de emitir token, sin normalizar el valor aceptado.
- No usar secretos embebidos fuera del sandbox ni registrar valores sensibles.
- Aislar emisión y validación del token del controlador HTTP para probar claims y expiración sin cambiar la API.

## Non-goals

- Añadir endpoints de login, introspección, refresh, revocación o logout.
- Cambiar el mecanismo de autenticación username/password, SQLite, el esquema de usuarios o el contrato de `/q/health`.
- Añadir claims no requeridos como roles, issuer, audience o scopes.
- Migrar o modificar consumidores externos en este cambio; deberán coordinar la aceptación del nuevo token y no recibir el secreto HMAC en código cliente.
- Rediseñar el manejo global de errores, CORS o la arquitectura de autenticación.

## Contratos protegidos

- Se preservan `POST /auth/login`, el cuerpo JSON, la autenticación mediante credenciales y los códigos/mensajes actuales para request nulo, username/password ausentes y credenciales inválidas.
- El éxito mantiene `200 OK` y los campos `token`, `expiresIn`, `user` y `debug`. `user` conserva `username`, `displayName` escapado y `role`; no expone contraseña, hash ni `client`.
- `expiresIn` permanece en segundos con valor nominal de 3600. El formato y contenido de `token` cambian intencionalmente; los consumidores lo tratarán como opaco.
- Cambios intencionales respecto de `docs/ENDPOINTS.md`: `client` deja de ser opcional y el token legacy reversible se reemplaza por un token firmado con claims y expiración verificables.

## Componentes afectados

- `LoginRequest`/`AuthResource`: validar `client`, mantener el orden de validación existente y delegar emisión del token a un servicio.
- `LegacyTokenUtil`: retirar su uso y eliminar el formato reversible y el secreto hardcodeado; no conservar una ruta de fallback que emita tokens legacy.
- Nuevo servicio de tokens: emitir y verificar el JWS, fijar los claims acordados y centralizar la duración; la operación de verificación será interna y no agregará una ruta HTTP.
- Configuración Quarkus: cargar `AUTH_TOKEN_SECRET` desde configuración externa, sin valor predeterminado integrado para perfiles no sandbox. Si falta la configuración requerida fuera del sandbox, fallar de forma segura y no emitir tokens.
- `pom.xml`: incorporar la biblioteca JWT seleccionada y mantenida, `com.auth0:java-jwt`, usando HS256 para la firma y verificación; no implementar primitivas criptográficas propias.
- Pruebas: ampliar `AuthResourceTest` y agregar pruebas unitarias del servicio de tokens con reloj controlable y secretos de prueba.

## Decisiones técnicas y alternativas

### Formato y firma

Elegir JWT/JWS compacto con HS256 usando `java-jwt`. HMAC conserva un secreto compartido y encaja con la configuración `AUTH_TOKEN_SECRET` del Issue, mientras que la biblioteca aporta serialización y verificación estándar sin construir manualmente el formato ni la criptografía. El secreto solo puede residir en servicios confiables que firmen o verifiquen; nunca se distribuye al frontend.

Alternativas consideradas:

- Mantener HMAC y crear un formato propio: rechazado porque el token actual es reversible y un formato casero aumentaría el riesgo de errores de firma, claims y expiración.
- Usar RS256 con clave privada/pública: ofrece verificación sin distribuir una clave de firma, pero requiere gestionar claves y configuración de publicación/rotación que no existen en el checkout. Reconsiderar si consumidores no confiables necesitan verificar tokens.
- Seguir con el token legacy o aceptar ambos formatos: rechazado porque el token actual no autentica integridad y el fallback prolongaría una credencial falsificable.

### Claims y tiempo

`sub` será el username autenticado; `client` será exactamente el valor no blanco enviado (se valida usando la regla de blancos existente, pero no se recorta ni normaliza). `iat` y `exp` serán NumericDate en segundos UTC. La duración continúa en 3600 segundos. La emisión calculará una sola marca temporal y derivará de ella `iat` y `exp`; `expiresIn` se derivará de la misma ventana para mantener la tolerancia de un segundo. El servicio de tokens recibirá un reloj inyectable/controlable para evitar pruebas dependientes del tiempo real.

La verificación comprobará firma y expiración, y rechazará el token cuando el tiempo actual sea igual o posterior a `exp`. No se agrega endpoint de validación ni se define issuer/audience en este cambio.

### Secreto y fallos de configuración

Leer `AUTH_TOKEN_SECRET` de configuración externa y no codificar un secreto de fallback. Fuera del sandbox, una configuración ausente o inválida impedirá emitir tokens y deberá detectarse al iniciar el servicio, no degradarse silenciosamente al formato legacy. Cualquier valor exclusivo de laboratorio debe estar segregado por perfil y documentado como tal. La distribución, rotación y almacenamiento operativo del secreto quedan como responsabilidad del entorno de despliegue; no se registrará el valor.

## Estrategia incremental

1. Añadir pruebas de caracterización del contrato HTTP actual antes de modificar el flujo: ruta/método, estructura y datos públicos de `user`, `debug`, duración, mensajes/status de request nulo y campos requeridos, y `401` por credenciales inválidas. Registrar también que hoy `client` se puede omitir y no se devuelve; esa observación debe cambiar intencionalmente al aplicar la spec.
2. Añadir pruebas unitarias del servicio de tokens para los cuatro claims, identidad/cliente, firma verificable, expiración y reloj límite; probar la ausencia de secreto sin emitir token. Usar secreto y reloj de prueba, nunca credenciales productivas.
3. Incorporar biblioteca y configuración de secreto, y crear el servicio aislado de emisión/verificación. No conectar todavía el controlador hasta que esas pruebas pasen.
4. Cambiar el flujo de login para validar `client` después de las validaciones username/password ya existentes y delegar emisión. Mantener la forma de la respuesta y eliminar la llamada y el secreto legacy.
5. Completar pruebas de integración: login válido, `client` ausente/blanco, credenciales inválidas, respuesta preservada, `expiresIn` frente a `exp`, token vencido y ausencia de valores sensibles en los logs.
6. Validar configuración por perfil y revisar que los consumidores reciban el nuevo token opaco. La coordinación de consumidores es un prerrequisito de despliegue, no una modificación a otros repositorios en este cambio.

Las pruebas de caracterización del paso 1 deben aprobar antes de tocar `AuthResource` o retirar `LegacyTokenUtil`. No se debe congelar el contenido legacy del token como contrato compatible: su reemplazo es un cambio intencional.

## Riesgos y trade-offs

- **Compatibilidad:** `client` pasa de opcional a obligatorio y los tokens previamente emitidos no tienen firma ni claims verificables. Clientes que omitan `client` o dependan del formato legacy deberán actualizarse y autenticarse de nuevo.
- **Secreto compartido:** HS256 requiere que cada verificador confiable conozca el mismo secreto y eleva el impacto de su filtración. No debe incluirse en Angular ni exponerse en logs o respuestas. Si ese límite no puede garantizarse, cambiar a firma asimétrica requiere una decisión y gestión de claves separadas.
- **Relojes:** emisores y verificadores con relojes desincronizados pueden discrepar en el límite de expiración. Mantener los relojes sincronizados y usar la tolerancia especificada únicamente al comparar `expiresIn`; no aceptar tokens después de `exp`.
- **Dependencia nueva:** la biblioteca reduce riesgo de formato/cripto manual, pero agrega una dependencia y su ciclo de actualización. Fijar una versión mantenida y revisar actualizaciones de seguridad.
- **Configuración:** retirar el fallback puede impedir el arranque de entornos que no definieron el secreto. Es un fallo cerrado intencional; documentar configuración por entorno antes de desplegar.
- **Cobertura de validación:** el backend actual solo emite tokens. La validación interna cubre la regla de expiración, pero los consumidores externos deben aplicar el mismo contrato antes de confiar en el token.

## Rollback

No habilitar aceptación dual ni restaurar silenciosamente el token legacy como fallback. Si el despliegue de la nueva emisión debe revertirse, volver al release anterior solo en el sandbox aislado, invalidar tokens/sesiones emitidos por la versión nueva y requerir nuevo login. En cualquier entorno fuera del sandbox, el rollback debe mantener el requisito de secreto configurado y no reintroducir el secreto hardcodeado; si los consumidores ya dependen de JWT, coordinar el rollback de sus versiones antes de revertir el emisor.
# Plan de arquitectura — HU-01: token firmado y claims mínimos

## Estado de aprobación

**Aprobado por el usuario el 2026-10-20 para HU-01.** La estrategia de token, cliente, tolerancia y secreto está decidida. Esta aprobación no convierte en aprobados otros candidatos de `HALLAZGOS.md`.

## Impacto

HU-01 afecta el contrato de login, la emisión del token consumido por `workshop-requirements-api`, la configuración del secreto y el tratamiento de información sensible en logs. La consulta de credenciales es un hallazgo independiente y no forma parte de HU-01. La base de datos, las rutas y la respuesta observable del login deben conservar compatibilidad. El proyecto está documentado como laboratorio local aislado.

## Componentes afectados

- `LegacyTokenUtil.create`: emisión de token consumido por la API.
- `AuthResource.login`: límite HTTP que valida la solicitud, consulta credenciales y construye la respuesta.
- `LoginRequest`: campo `client` recibido desde la solicitud.
- Configuración de `AUTH_TOKEN_SECRET` y logging relacionado con autenticación.
- `AuthResourceTest`: prueba actual de login exitoso.

El control de identidad y autorización de tareas pertenece a la API de requisitos y se planifica en el `Plan-Architecture.md` de ese repositorio.

## Contratos protegidos

- `POST /auth/login` recibe JSON con `username` y `password`; el frontend agrega `client`.
- El éxito continúa siendo compatible con el frontend: respuesta 2xx con `token` no vacío y objeto `user`.
- `expiresIn` se conserva y debe ser coherente con el claim `exp` dentro de 5 segundos.
- Credenciales válidas continúan permitiendo el login; las inválidas no deben autenticarse.
- El token conserva interoperabilidad con la API consumidora; los claims mínimos aprobados son `sub`, `client`, `iat` y `exp`.
- Solo se acepta el `client` enviado por el frontend (`angular-workshop`); ausencia u otro valor se rechaza con `400`.
- No se registran tokens, secretos ni valores sensibles.
- No cambiar interfaz, almacenamiento de sesión ni comportamiento de navegación del frontend.

## Estrategia incremental

1. Caracterizar login válido/inválido, body enviado por el frontend y respuesta esperada, incluidos `token`, `expiresIn` y `user`.
2. Emitir un JWT HS256 con `sub`, `client`, `iat`, `exp` y el claim actual `role`; aceptar únicamente `client=angular-workshop`.
3. Requerir `AUTH_TOKEN_SECRET` en todos los entornos y rechazar configuración ausente o de menos de 32 bytes UTF-8.
4. Coordinar con `workshop-requirements-api` el rechazo inmediato del formato Base64 anterior y la validación de HS256, claims y expiración.
5. Mantener la respuesta login y calcular `expiresIn` a partir del mismo `exp`; comprobar diferencia máxima de 5 segundos.
6. Revisar los logs del flujo y comprobar que no incluyen token, secreto ni valores sensibles.
7. Mantener las pruebas de login válido/inválido y añadir pruebas de claims, expiración, cliente, configuración y logging.
8. Ejecutar pruebas de contrato del frontend y pruebas de integración emisor-consumidor antes de considerar el cambio listo.

La corrección de SQL de `LegacyDatabase.findByCredentials` queda fuera de HU-01 y debe rastrearse como alcance separado.

## Pruebas necesarias

- `AuthResourceTest`: login válido, credenciales inválidas y aceptación del `client` esperado.
- Claims: token válido contiene `sub`, `client`, `iat` y `exp`; `client` refleja el valor validado de la solicitud.
- Expiración: token expirado se rechaza y `expiresIn` es coherente con `exp - now` dentro de la tolerancia aprobada.
- Configuración: comprobar que la aplicación no inicia con el secreto ausente o débil y documentar `AUTH_TOKEN_SECRET` sin valor predeterminado.
- Logging: comprobación de que tokens, secretos y valores sensibles no aparecen en los logs del flujo.
- Prueba de respuesta compatible con `readLoginResponse`: token no vacío y objeto `user`.
- Pruebas de integración con `workshop-requirements-api`: aceptación de token válido y rechazo de token alterado/expirado.
- Pruebas del cliente para confirmar que el método, ruta, body y manejo de respuestas HTTP permanecen sin cambios.

## Riesgos / trade-offs

- Actualizar emisor y consumidor separadamente puede interrumpir solicitudes legítimas mientras haya tokens de versiones distintas.
- Mantener el contrato JSON limita los cambios visibles, pero no resuelve por sí mismo la política de expiración o revocación.
- El rechazo inmediato de tokens Base64 anteriores invalida sesiones todavía activas; el emisor y el consumidor deben desplegarse coordinadamente.
- Solo se admite el cliente aprobado `angular-workshop`; clientes distintos deben coordinarse antes de solicitar soporte.
- Emisor y consumidor usan `exp` como límite estricto de expiración y una tolerancia acordada de 5 segundos únicamente al comparar `expiresIn` con `exp - now`.
- El entorno se declara local y aislado; esta planificación no autoriza despliegue público ni amplía el alcance a otros hallazgos.

## Decisiones abiertas

No quedan decisiones abiertas para el alcance de HU-01.

## Decisión arquitectónica — JWT HS256 interoperable

- **Contexto:** el backend Java emite tokens que la API Python valida; el formato Base64 actual no proporciona integridad verificable.
- **Opciones consideradas:** conservar un formato HMAC propio; JWT con HS256 y secreto compartido; JWT asimétrico con par de claves.
- **Decisión:** usar JWT estándar firmado con HS256, mediante SmallRye JWT Build en Java y PyJWT en Python. El secreto `AUTH_TOKEN_SECRET` se requiere en ambos servicios en todos los entornos, sin fallback, con al menos 32 bytes UTF-8. Validar solo `client=angular-workshop`, comparar `expiresIn` con `exp - now` con tolerancia de 5 segundos y rechazar inmediatamente tokens Base64 antiguos.
- **Consecuencias:** ambos servicios deben desplegarse coordinadamente y tener el mismo secreto; el cambio invalida de inmediato tokens anteriores. HS256 simplifica la interoperabilidad del laboratorio, pero exige proteger y distribuir un secreto compartido. La API no aplica tolerancia tras el claim `exp`.

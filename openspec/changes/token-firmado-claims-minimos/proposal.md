# Proposal

## Why

El Issue [#12](https://github.com/user-bcp-student49/workshop-auth-backend/issues/12) señala que el login emite un token sin firma y sin claims verificables de cliente y tiempo. Definir un contrato firmado, con expiración coherente y sin exposición de valores sensibles permite que los consumidores comprueben la identidad y vigencia del token sin alterar innecesariamente el contrato HTTP existente.

## What Changes

- El login exitoso emitirá un token firmado con los claims `sub`, `client`, `iat` y `exp`; el Issue deja pendiente la decisión técnica entre HMAC y una biblioteca JWT.
- **BREAKING**: `client`, actualmente opcional según `docs/ENDPOINTS.md`, deberá estar presente y no ser vacío o solo espacios para emitir un token. El valor aceptado se reflejará en el claim `client`.
- `expiresIn` seguirá expresándose en segundos y deberá concordar con `exp - now` dentro de una tolerancia documentada de un segundo.
- Un token vencido no se considerará válido. Esta semántica y los casos de login válido, credenciales inválidas y token vencido tendrán escenarios verificables.
- Fuera del sandbox no se usará un secreto predeterminado; cualquier valor predeterminado de laboratorio deberá identificarse expresamente como exclusivo del sandbox. Los logs no expondrán tokens, secretos ni otros valores sensibles.
- Se preservan la ruta y método `POST /auth/login`, la autenticación username/password, la forma del objeto `user`, el campo `debug` y las respuestas existentes para request nulo, username/password requeridos y credenciales inválidas. El valor y representación de `token` cambian al incorporar claims y firma; el cliente debe tratarlo como opaco.

**Supuestos para esta propuesta:** `client` es válido si no es nulo, vacío ni solo espacios; no se introduce una allowlist ni otra restricción de formato. La tolerancia de expiración propuesta es de un segundo para hacer verificable la coherencia temporal sin fijar una implementación.

## Capabilities

### New Capabilities
- `auth-token`: contrato de emisión, claims, expiración y protección del token devuelto por login.

### Modified Capabilities
- Ninguna. El repositorio no tiene specs existentes.

## Impact

- API `POST /auth/login`: nuevo requisito para `client` y nuevas garantías del token; se preserva la estructura restante documentada en `docs/ENDPOINTS.md`.
- Configuración de `AUTH_TOKEN_SECRET` y tratamiento de logs sensibles.
- Casos verificables de login válido, credenciales inválidas y expiración. La decisión HMAC frente a biblioteca JWT queda fuera de esta propuesta.
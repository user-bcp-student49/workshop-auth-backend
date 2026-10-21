# Spec Delta

## Purpose

Define el contrato observable de los tokens emitidos por el login: identidad, cliente, firma y vigencia. La capability también establece límites para la exposición de secretos y preserva el contrato HTTP de autenticación que no cambia con el Issue #12.

## ADDED Requirements

### Requirement: El login requiere un cliente no vacío
El sistema SHALL exigir un valor `client` no nulo, no vacío y no compuesto solo por espacios para emitir un token. Esta nueva condición modifica el contrato actual, donde `client` es opcional.

#### Scenario: Client ausente con credenciales válidas
- **WHEN** se envían credenciales válidas sin `client`
- **THEN** el login responde `400 Bad Request` y no emite un token

#### Scenario: Client compuesto solo por espacios
- **WHEN** se envían credenciales válidas con `client` vacío o compuesto solo por espacios
- **THEN** el login responde `400 Bad Request` y no emite un token

### Requirement: El token firmado contiene los claims mínimos
Ante un login exitoso, el token SHALL estar firmado y contener `sub`, `client`, `iat` y `exp`. `sub` SHALL identificar al usuario autenticado y `client` SHALL coincidir con el valor aceptado de la solicitud. El algoritmo y formato del token no forman parte de este requisito.

#### Scenario: Login exitoso emite identidad y cliente
- **WHEN** se envían credenciales válidas y un `client` válido
- **THEN** la respuesta `200 OK` incluye un token cuya firma puede verificarse y cuyos claims `sub` y `client` coinciden con el usuario autenticado y el cliente enviado
- **AND** el token contiene los claims `iat` y `exp`, con `exp` posterior a `iat`

### Requirement: La expiración determina la vigencia del token
El claim `exp` SHALL marcar el fin de vigencia del token. Una validación de token SHALL rechazarlo cuando el tiempo actual sea igual o posterior a `exp`.

#### Scenario: Token vencido
- **WHEN** se valida un token firmado cuyo `exp` ya pasó
- **THEN** la validación lo rechaza como vencido

### Requirement: expiresIn concuerda con la expiración
La respuesta de login SHALL expresar `expiresIn` en segundos y su valor SHALL concordar con `exp - now` al emitir la respuesta, con una tolerancia máxima de un segundo.

#### Scenario: Duración declarada coincide con la del token
- **WHEN** un login exitoso devuelve `expiresIn` y un token con `exp`
- **THEN** la diferencia entre `expiresIn` y el tiempo restante hasta `exp` al emitir la respuesta no supera un segundo

### Requirement: Se preserva el contrato HTTP no afectado por el cambio de token
El sistema SHALL conservar `POST /auth/login`, la autenticación con `username` y `password`, y la estructura de respuesta exitosa documentada, excepto por el valor y semántica del token y la nueva validación de `client`. SHALL conservar también los errores documentados de request nulo, username/password requeridos y credenciales inválidas.

#### Scenario: Login exitoso conserva los datos públicos de usuario
- **WHEN** se envían credenciales y `client` válidos
- **THEN** el login responde `200 OK` con `token`, `expiresIn`, `user` y `debug`
- **AND** `user` conserva `username`, `displayName` escapado y `role`, sin devolver contraseña, hash ni `client`

#### Scenario: Credenciales inválidas conservan el error
- **WHEN** se envía `username` o `password` incorrecto y un `client` válido
- **THEN** el login responde `401 Unauthorized` con el error de autenticación documentado y no emite un token

#### Scenario: Validaciones existentes de request se conservan
- **WHEN** el cuerpo es nulo, falta `username` o falta `password`
- **THEN** el login conserva las respuestas `400 Bad Request` y los mensajes documentados para esas condiciones

### Requirement: Los secretos y tokens no se exponen
Fuera del sandbox, el sistema SHALL emitir tokens únicamente cuando se haya configurado un secreto y SHALL NOT usar un secreto predeterminado integrado. Los logs SHALL NOT incluir tokens, secretos, contraseñas ni otros valores sensibles de autenticación.

#### Scenario: Secreto no configurado fuera del sandbox
- **WHEN** el servicio opera fuera del sandbox sin un secreto configurado
- **THEN** no emite un token firmado con un secreto predeterminado

#### Scenario: Emisión y rechazo de autenticación no filtran valores sensibles
- **WHEN** se procesa un login exitoso o fallido
- **THEN** los logs no contienen el token, el secreto configurado, la contraseña ni otros valores sensibles de autenticación
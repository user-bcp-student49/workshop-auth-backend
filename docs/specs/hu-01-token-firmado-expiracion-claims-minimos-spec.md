# HU-01 — Token firmado con expiración y claims mínimos

## 1. Summary
- Actualizar el token de login para incluir `sub`, `client`, `iat` y `exp`; mantener `expiresIn` coherente con la expiración del token y evitar registrar tokens, secretos o valores sensibles.
- Documentar la decisión entre conservar HMAC o adoptar una biblioteca JWT.

## 2. Alcance
- **Dentro del alcance**
  - Incluir los claims requeridos en un token válido.
  - Obtener y validar `client` desde la solicitud de login e incluirlo en el token.
  - Mantener `expiresIn` de la respuesta coherente con `exp - now`, dentro de una tolerancia documentada.
  - Resolver el comportamiento del secreto predeterminado fuera del sandbox o documentar que solo se usa en el laboratorio.
  - Probar el login válido, las credenciales inválidas y los tokens expirados; evitar que se registren valores sensibles.
  - Documentar la decisión entre HMAC y una biblioteca JWT, así como la configuración de `AUTH_TOKEN_SECRET`.
- **Fuera del alcance**
  - HU-01 no especifica elementos fuera del alcance.

## 3. Supuestos
- HU-01 describe la implementación actual como una que usa HMAC y lee `AUTH_TOKEN_SECRET`.
- La respuesta de login incluye `token`, `expiresIn` y `user`.
- No se considera decidido ningún formato de token, cambio de algoritmo, regla de validación de `client` ni valor de tolerancia, salvo que se confirme explícitamente en la HU.

## 4. Decisiones técnicas por tomar
- Decidir si se conserva HMAC o se adopta una biblioteca JWT y documentar la decisión.
- Definir cómo se valida `client`.
- Definir la tolerancia documentada para comparar `expiresIn` con `exp - now`.
- Decidir si se elimina el secreto predeterminado fuera del sandbox o si se conserva documentándolo explícitamente como exclusivo del laboratorio.
- Especificar los requisitos de configuración y manejo de `AUTH_TOKEN_SECRET`.

## 5. Riesgos y mitigaciones
- **Claims del token o expiración de la respuesta inconsistentes:** probar los claims requeridos y verificar `expiresIn` frente a `exp - now` usando la tolerancia acordada.
- **Inclusión de datos de `client` sin validar en el token:** definir y probar la validación de `client` antes de emitir el token.
- **Exposición de tokens, secretos o valores sensibles en logs:** inspeccionar el comportamiento del logging y conservar evidencia de que esos valores no se registran.
- **Uso accidental de secretos predeterminados fuera del sandbox:** resolver y documentar la decisión sobre el secreto predeterminado y la configuración de `AUTH_TOKEN_SECRET`.

## 6. Criterios de aceptación
- Un token válido contiene `sub`, `client`, `iat` y `exp`.
- `client` se obtiene de la solicitud, se valida según las reglas acordadas y se refleja en el token.
- `expiresIn` coincide con `exp - now` dentro de la tolerancia documentada.
- Hay pruebas para login válido, credenciales inválidas y un token expirado.
- Los logs no contienen tokens, secretos ni valores sensibles.
- La decisión entre HMAC y una biblioteca JWT está documentada.
- La configuración de `AUTH_TOKEN_SECRET` está documentada y cualquier valor predeterminado se elimina fuera del sandbox o se documenta como exclusivo del laboratorio.

## 7. Plan de validación
- **Pruebas unitarias**
  - Verificar los claims requeridos en un token válido.
  - Verificar la validación de `client` y el valor incluido en el claim.
  - Verificar la expiración y la coherencia de `expiresIn` dentro de la tolerancia documentada.
- **Pruebas funcionales**
  - Cubrir el login válido y las credenciales inválidas.
  - Verificar que se rechace un token expirado.
- **Comprobaciones de seguridad**
  - Inspeccionar los logs para detectar exposición de tokens, secretos o valores sensibles.
  - Verificar el comportamiento documentado del secreto predeterminado y la configuración de `AUTH_TOKEN_SECRET`.
- **Evidencia requerida**
  - Pruebas de claims, expiración y errores de autenticación.
  - Documentación de `AUTH_TOKEN_SECRET` y de la decisión entre HMAC y JWT.
  - Evidencia de que los logs están redactados.
  - Esta especificación generada a partir de HU-01.

## 8. Tareas sugeridas por rol
- **Chapter/PO**
  - Confirmar el origen esperado y el significado de `client`.
  - Confirmar la tolerancia requerida para la coherencia de la expiración.
  - Revisar la evidencia de aceptación documentada.
- **Tech Lead**
  - Registrar la decisión entre HMAC y una biblioteca JWT.
  - Resolver la tolerancia de expiración y la validación de `client`.
  - Revisar el comportamiento del secreto predeterminado en relación con los límites del sandbox.
- **Backend**
  - Implementar los claims requeridos con el enfoque de token que se decida.
  - Validar `client` y alinear `expiresIn` con la expiración del token.
  - Asegurar que no se registren tokens ni secretos; documentar la configuración.
- **Frontend**
  - HU-01 no especifica cambios en el frontend; se debe confirmar si aplican.
- **QA**
  - Probar el login válido, las credenciales inválidas y el comportamiento de tokens expirados.
  - Verificar los claims requeridos y la coherencia de la expiración.
  - Adjuntar evidencia de pruebas y de revisión de logs.
- **Cyber**
  - Revisar el manejo del secreto de firma y cualquier valor predeterminado fuera del sandbox.
  - Comprobar que los logs no expongan tokens ni secretos.
  - Revisar la evidencia de claims y comportamiento de expiración.

## 9. Preguntas abiertas
- ¿Qué reglas exactas definen un valor válido de `client` y qué debe ocurrir si falta o no es válido?
- ¿Qué tolerancia de expiración debe documentarse?
- ¿Se debe conservar HMAC o adoptar una biblioteca JWT?
- ¿Se debe eliminar el secreto predeterminado fuera del sandbox o conservarlo documentándolo como exclusivo del laboratorio?
- ¿Cuáles son los requisitos de configuración, rotación y despliegue de `AUTH_TOKEN_SECRET`?
- ¿Se requiere algún cambio en el frontend o el consumidor ya proporciona `client`?
- ¿Qué consumidores del token deben validar los claims y la expiración resultantes?

# HU-01 — Token firmado con expiración y claims mínimos

## Estado de aprobación
**Aprobada por el usuario el 2026-10-20.** Las decisiones técnicas de token registradas aquí también fueron confirmadas: JWT HS256, SmallRye JWT Build en Java, PyJWT en Python, `client=angular-workshop`, tolerancia de 5 segundos, secreto obligatorio y rechazo inmediato del token anterior.

## 1. Summary
- Actualizar el token de login a JWT firmado con HS256 e incluir `sub`, `client`, `iat` y `exp`; mantener `expiresIn` coherente con la expiración y evitar registrar tokens, secretos o valores sensibles.
- Usar SmallRye JWT Build en Java y PyJWT en Python, con contrato interoperable y validación explícita de algoritmo.

## 2. Alcance
- **Dentro del alcance**
  - Incluir los claims requeridos en un token válido.
  - Obtener y validar `client` desde la solicitud de login e incluirlo en el token.
  - Mantener `expiresIn` coherente con `exp - now` dentro de una tolerancia de 5 segundos.
  - Requerir `AUTH_TOKEN_SECRET` en todos los entornos, sin valor predeterminado, y con longitud mínima de 32 bytes UTF-8.
  - Probar el login válido, las credenciales inválidas y los tokens expirados; evitar que se registren valores sensibles.
  - Rechazar inmediatamente tokens Base64 previos, sin validación dual.
  - Documentar las bibliotecas, HS256 y la configuración de `AUTH_TOKEN_SECRET`.
- **Fuera del alcance**
  - HU-01 no especifica elementos fuera del alcance.

## 3. Supuestos
- La implementación actual codifica claims con Base64 y un secreto constante; no genera un JWT firmado.
- La respuesta de login incluye `token`, `expiresIn` y `user`.
- El frontend envía `client=angular-workshop`.
- Se mantiene el claim adicional `role` para conservar la identidad que consume actualmente la API; los claims mínimos son `sub`, `client`, `iat` y `exp`.

## 4. Decisiones técnicas aprobadas
- Usar JWT HS256 con SmallRye JWT Build en Java y PyJWT en Python.
- Aceptar solo `client=angular-workshop`; campo ausente o distinto se rechaza con HTTP 400.
- Comparar `expiresIn` con `exp - now` con tolerancia máxima de 5 segundos; el consumidor rechaza el token en `exp` sin leeway adicional.
- Requerir `AUTH_TOKEN_SECRET` en todos los entornos, sin secreto predeterminado y con un mínimo de 32 bytes UTF-8.
- Rechazar inmediatamente el formato Base64 anterior; no habrá periodo de doble validación.

## 5. Riesgos y mitigaciones
- **Claims del token o expiración de la respuesta inconsistentes:** probar los claims requeridos y verificar `expiresIn` frente a `exp - now` usando la tolerancia acordada.
- **Inclusión de datos de `client` sin validar en el token:** aceptar y firmar solo `angular-workshop`; probar rechazo con HTTP 400 para otros valores y ausencia.
- **Exposición de tokens, secretos o valores sensibles en logs:** inspeccionar el comportamiento del logging y conservar evidencia de que esos valores no se registran.
- **Secreto ausente o débil:** exigir `AUTH_TOKEN_SECRET` en todos los entornos y verificar que tiene al menos 32 bytes UTF-8; no usar un default.

## 6. Criterios de aceptación
- Un token válido contiene `sub`, `client`, `iat` y `exp`.
- El token está firmado con HS256 y es interoperable entre Java y Python.
- `client` se obtiene de la solicitud; solo se acepta `angular-workshop`, que se refleja en el token.
- `expiresIn` coincide con `exp - now` dentro de 5 segundos.
- La aplicación falla al iniciar si `AUTH_TOKEN_SECRET` falta o tiene menos de 32 bytes UTF-8.
- Hay pruebas para login válido, credenciales inválidas y un token expirado.
- Los logs no contienen tokens, secretos ni valores sensibles.
- Los tokens Base64 previos se rechazan inmediatamente.
- La configuración de `AUTH_TOKEN_SECRET` está documentada y no hay valor predeterminado.

## 7. Plan de validación
- **Pruebas unitarias**
  - Verificar los claims requeridos en un token válido.
  - Verificar que solo `client=angular-workshop` se acepta y se incluye en el claim.
  - Verificar expiración estricta y coherencia de `expiresIn` dentro de 5 segundos.
- **Pruebas funcionales**
  - Cubrir el login válido y las credenciales inválidas.
  - Verificar que se rechace un token expirado.
- **Comprobaciones de seguridad**
  - Inspeccionar los logs para detectar exposición de tokens, secretos o valores sensibles.
  - Verificar que `AUTH_TOKEN_SECRET` es obligatorio y no tiene valor predeterminado.
- **Evidencia requerida**
  - Pruebas de claims, expiración y errores de autenticación.
  - Documentación de `AUTH_TOKEN_SECRET`, JWT HS256 y las bibliotecas interoperables.
  - Evidencia de que los logs están redactados.
  - Esta especificación generada a partir de HU-01.

## 8. Tareas sugeridas por rol
- **Chapter/PO**
  - Revisar la evidencia de aceptación documentada para `client=angular-workshop` y la tolerancia de 5 segundos.
- **Tech Lead**
  - Mantener la decisión HS256 interoperable y revisar el comportamiento del secreto obligatorio.
  - Revisar los criterios aprobados para `client` y la tolerancia de expiración.
- **Backend**
  - Implementar los claims requeridos con JWT HS256 usando SmallRye JWT Build.
  - Validar `client` y alinear `expiresIn` con la expiración del token.
  - Asegurar que no se registren tokens ni secretos; documentar la configuración.
- **Frontend**
  - No se requieren cambios de implementación; el cliente actual ya envía `client=angular-workshop` y consume el token como string opaco.
- **QA**
  - Probar el login válido, las credenciales inválidas y el comportamiento de tokens expirados.
  - Verificar los claims requeridos y la coherencia de la expiración.
  - Adjuntar evidencia de pruebas y de revisión de logs.
- **Cyber**
  - Revisar el manejo del secreto obligatorio y verificar que no exista un fallback.
  - Comprobar que los logs no expongan tokens ni secretos.
  - Revisar la evidencia de claims y comportamiento de expiración.

## 9. Preguntas abiertas
- ¿Qué política de rotación y despliegue de `AUTH_TOKEN_SECRET` se usará fuera del laboratorio?
- ¿Existe algún consumidor adicional del token además de `workshop-requirements-api`?

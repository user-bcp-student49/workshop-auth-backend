# Autenticación y límites de tareas — backend de autenticación

## Fuente

Hallazgos aprobados por el usuario a partir de `HALLAZGOS.md`:

- Consulta de autenticación construida con credenciales concatenadas, evidencia en `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:32,35`.
- Hallazgo de integridad del token entre el emisor y `workshop-requirements-api`, documentado en `../workshop-requirements-api/HALLAZGOS.md`, evidencia del emisor en `src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java:8,14-15`.

Los hallazgos históricos no aprobados en las matrices previas no forman parte de esta especificación.

## Contexto actual

`POST /auth/login` acepta credenciales JSON. `LegacyDatabase.findByCredentials` construye la consulta de autenticación concatenando `username` y `password`. Al autenticar, el servicio devuelve un token consumido por la API de requisitos. El README identifica este backend como un laboratorio local aislado.

## Problema confirmado

El procesamiento de credenciales no garantiza que sus valores se interpreten solamente como datos. Además, el formato actual del token debe poder ofrecer identidad verificable al servicio consumidor para que este pueda rechazar tokens alterados.

## Comportamiento a preservar

- Mantener `POST /auth/login` y la recepción JSON de `username` y `password`; el frontend agrega el campo `client`.
- Mantener la respuesta de login compatible con el frontend: `token` no vacío y objeto `user`.
- Mantener la interoperabilidad del token entre este backend y `workshop-requirements-api`.
- Mantener la distinción observable entre login válido e inválido; la prueba del backend espera `200` en éxito y el flujo actual usa `401` para credenciales inválidas.
- Mantener el uso local y aislado indicado en el README.

## Alcance

- Definir el comportamiento de autenticación para que caracteres especiales en las credenciales no alteren la semántica de la búsqueda.
- Definir el requisito de que el token emitido permita al servicio consumidor comprobar la integridad y vigencia de la identidad, de forma compatible con el contrato acordado entre servicios.

## Fuera de alcance

- Cambios en la interfaz, sesión o navegación del frontend.
- Cambios de esquema de usuarios, política de contraseñas o funciones administrativas.
- Selección de una biblioteca, algoritmo o formato concreto para tokens.
- Despliegue público o cambios a la política de exposición del laboratorio.
- Hallazgos que no estén aprobados como fuente de esta especificación.

## Criterios de aceptación

1. Pruebas de login demuestran que valores de credenciales con caracteres especiales se procesan como datos literales y no convierten un intento inválido en autenticación exitosa.
2. Una credencial de usuario válida continúa permitiendo el login y una credencial inválida continúa siendo rechazada.
3. El token emitido es aceptado por la API de requisitos cuando es válido y vigente.
4. Un token emitido cuyo contenido de identidad o vigencia haya sido alterado es rechazado por la API de requisitos.
5. La respuesta de éxito continúa exponiendo los campos que consume el frontend, incluido un `token` no vacío y `user`.
6. Las pruebas de contrato de ambos servicios verifican conjuntamente la emisión y validación del token sin interrumpir el login legítimo ni el acceso legítimo a tareas.

## Contratos afectados/protegidos

- `POST /auth/login`: JSON con `username` y `password`; el frontend también envía `client`. El cliente acepta respuestas 2xx que incluyan `token` no vacío y un objeto `user`.
- Respuesta de login consumida por `AuthService` y validada por `readLoginResponse`.
- Token compartido por el backend y la API mediante `Authorization: Bearer <token>`.
- La autenticación de tareas permanece bajo la API; este backend emite la identidad consumida, pero no define por sí solo la autorización de propietarios.

## Riesgos, supuestos y preguntas abiertas

- Se asume que ambos servicios pueden actualizarse coordinadamente y que la compatibilidad del token se prueba entre ellos.
- Debe acordarse el comportamiento durante una transición de token para no invalidar sesiones emitidas por versiones anteriores antes de lo previsto.
- El README restringe el proyecto a un entorno local aislado; la prioridad y requisitos de exposición cambian si se aprueba otro entorno.
- No se define aquí un nuevo esquema de error; los códigos y cuerpos deben mantenerse compatibles con los consumidores existentes.

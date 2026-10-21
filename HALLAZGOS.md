# Hallazgos del backend de autenticación

Los hallazgos se limitan a los elementos enumerados en `RETO.md`. La severidad expresa el riesgo observado en este repositorio; no presupone que un consumidor externo del token o una dependencia vulnerable hayan sido verificados.

| id | hallazgo | tipo | explicacion | evidencia (archivo:linea) | severidad | estado |
|---|---|---|---|---|---|---|
| H-01 | Concatenación de credenciales en SQL | Vulnerabilidad | El usuario y la contraseña se insertan directamente en la consulta. Una entrada manipulada puede alterar la autenticación y permitir acceso no autorizado. | `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:31-35` | alta | Pendiente |
| H-02 | Contraseñas almacenadas en texto plano | Vulnerabilidad | Una lectura no autorizada de la base expone las contraseñas almacenadas directamente, facilitando su reutilización en otros servicios. | `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:22-24` | media | Pendiente |
| H-03 | Token Base64 con secreto embebido y sin firma | Vulnerabilidad | Base64 solo codifica el contenido; no protege su integridad. El token puede leerse y modificarse. El impacto de autorización depende de cómo lo validen los consumidores, que no se verifica en este repositorio. | `src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java:8,12-15` | media | Pendiente |
| H-04 | Secreto hardcodeado y CORS permisivo | Vulnerabilidad | El secreto está incluido en el código y CORS permite cualquier origen, lo que expone la configuración del token y amplía innecesariamente los orígenes autorizados. | `src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java:8`; `src/main/resources/application.properties:4-7` | media | Pendiente |
| H-05 | Versiones de dependencias señaladas para SCA | Sugerencia | `RETO.md` señala Quarkus `3.27.4` y Commons Text `1.9`. Esas versiones están declaradas; este análisis no verifica advisories ni demuestra explotación, por lo que debe confirmarse el riesgo con evidencia SCA. | `pom.xml:13,48-50` | baja | Pendiente |
| H-06 | Respuesta de error expone detalles internos | Vulnerabilidad | Una excepción que alcance el `catch` general puede devolver al cliente el mensaje y la clase de excepción, revelando detalles de implementación. | `src/main/java/com/bcp/workshop/auth/AuthResource.java:54-58` | baja | Pendiente |
| H-07 | `AuthResource.login` combina varias responsabilidades | Refactor | El método realiza validación, acceso a persistencia, generación del token y construcción de respuesta; esto dificulta aislar y probar los cambios del flujo de autenticación. | `src/main/java/com/bcp/workshop/auth/AuthResource.java:24-58` | baja | Pendiente |

## Análisis discovery — 2026-10-20

Proyecto: `workshop-auth-backend`. Alcance: implementación y prueba del endpoint `POST /auth/login`.
Los registros anteriores de este archivo se conservan como historial; los candidatos de esta ejecución son los siguientes.

### Consulta de autenticación construida con credenciales concatenadas
- Estado: CANDIDATO
- Evidencia: `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:32` concatena `username` y `password` en el SQL; la consulta se ejecuta en la línea 35 con `Statement.executeQuery(sql)`.
- Impacto: una entrada puede alterar la semántica de la consulta de autenticación y comprometer el contrato de acceso al login.
- Prioridad propuesta: P1 (alta si el endpoint se expone fuera del entorno local aislado descrito en el README).
- Contrato potencialmente afectado: `POST /auth/login`, en particular la distinción entre autenticación válida (`200`) e inválida (`401`).
- Cómo validarlo: añadir pruebas con valores de credenciales que incluyan caracteres especiales y comprobar que se tratan como datos literales, sin cambiar el resultado de autenticación ni producir respuestas de éxito inesperadas.

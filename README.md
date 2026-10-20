# Workshop Auth Backend — Java Quarkus

> **Laboratorio intencionalmente vulnerable.** Ejecutar únicamente en local, repositorios privados y ambientes aislados. No desplegar en Internet, CERT o PROD.

Backend Quarkus que autentica usuarios de laboratorio almacenados en SQLite y entrega un token consumido por el frontend Angular y la API Python.

## Ejecución

Requisitos: JDK 21 y Maven 3.9+.

Configura `AUTH_TOKEN_SECRET` antes de iniciar el servicio. Debe ser el mismo
secreto configurado en `workshop-requirements-api` y contener al menos 32
bytes UTF-8. No existe un valor predeterminado; el servicio no inicia si
falta o es demasiado corto. No compartas el secreto ni lo guardes en el repo.

```bash
mvn quarkus:dev
```

Servicio: `http://localhost:8080`

Credenciales locales:

- `student` / `Workshop123!`
- `trainer` / `Trainer123!`

## Endpoints

- `POST /auth/login`
- `GET /q/health`

## Propósito de la rama `main`

La rama contiene deuda técnica y de seguridad deliberada: SQL concatenado,
contraseñas en texto plano, CORS permisivo, método largo, manejo genérico de
excepciones y dependencias con CVEs. El token de login usa JWT HS256 con los
claims `sub`, `client`, `iat` y `exp`; `client` debe ser `angular-workshop`.
Los tokens Base64 del laboratorio anterior ya no se aceptan.

# ms-barriodigital-bff

Backend-for-Frontend de BarrioDigital. Es el **único microservicio expuesto** al
frontend (vía API Gateway): valida el JWT de Azure AD (issuer, audience, firma,
expiración) en cada request y hace de proxy hacia `ms-barriodigital-requests` y
`ms-barriodigital-catalog`.

## Cómo correr local

Requiere Java 17 y Maven.

```bash
./mvnw spring-boot:run
```

Levanta en `http://localhost:8080`. Necesita que `ms-barriodigital-requests`
(`:8081`) y `ms-barriodigital-catalog` (`:8082`) estén corriendo, y un tenant de
Azure AD real para validar tokens (ver variables abajo).

Tests: `./mvnw verify` — corren con JWT mockeado (`application-test.yml`), no
necesitan Azure AD real.

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `MS_REQUESTS_URL` | `http://localhost:8081` | URL base de `ms-barriodigital-requests` |
| `MS_CATALOG_URL` | `http://localhost:8082` | URL base de `ms-barriodigital-catalog` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Orígenes permitidos (coma-separado) |
| `AAD_ISSUER_URI` | — | `https://login.microsoftonline.com/<TENANT_ID>/v2.0` |
| `AAD_API_CLIENT_ID` | — | Client ID del App Registration `barriodigital-api` |
| `AAD_REQUIRED_SCOPE` | `access_as_user` | Scope exigido en el token |

## Docker

```bash
docker build -t ms-barriodigital-bff .
docker run -p 8080:8080 --env-file .env ms-barriodigital-bff
```

Imagen publicada automáticamente en cada push a `main`:
`ghcr.io/phamnukz/ms-barriodigital-bff:latest`.

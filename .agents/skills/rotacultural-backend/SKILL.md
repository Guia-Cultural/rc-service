---
name: rotacultural-backend
description: >-
  Use when modifying or testing endpoints, services, security rules, database
  migrations, or image storage (FileSystemStorageService and AzureBlobStorageService)
  in rc-service.
---

# Rota Cultural Backend Workflow (`rc-service`)

## Objectives
Keep Spring Boot endpoints simple, secure, ownership-checked, and compatible with both local MySQL dev and Azure production (`prod` profile + `AzureBlobStorageService`).

## Procedure
1. **Endpoints & DTOs**: Place `@RestController` routes under `controllers/` and Jakarta-validated request/response records or POJOs under `dtos/`. Never return password hashes or internal stack traces.
2. **Entities & Repositories**: Map columns accurately against `src/main/resources/db/migration/V1__baseline_schema.sql` and `V2__mvp_evolution.sql` so `prod` (`ddl-auto=validate`) stays compatible.
3. **Permissions & Ownership**:
   - Configure URL matchers in `SecurityConfig.java` obeying hierarchy `ADMIN > NATIVO > USUARIO` (keep specific `/places/my` and `/places/admin` before `/places/*`).
   - Enforce resource ownership inside the service using `authService.getCurrentUser()`, `authService.isAdmin()`, and `authService.requireOwnerOrAdmin(...)`.
4. **Storage (`StorageService`)**:
   - Inject `StorageService` (implemented by `FileSystemStorageService` when `app.storage.provider=local` or `AzureBlobStorageService` when `azure`).
   - Always run `ImageFileValidator` (JPG/JPEG/PNG/WEBP, <= 5MB, magic-byte check, UUID filename, max 10 images per place).
   - Store canonical `/uploads/places/{placeId}/{uuid}.{ext}` URLs in MySQL; serve public `APROVADO` photos openly and gate `EM_AVALIACAO`/`REJEITADO` photos via `MediaController` (creator, `ADMIN`, or short-lived HMAC signature).

## Validation
- Run integration suite:
  `.\mvnw.cmd test` (uses `application-test.properties` against `rotacultural_test`).
- Package JAR:
  `.\mvnw.cmd -DskipTests package`
- Verify `Tests run: ..., Failures: 0, Errors: 0` before committing.

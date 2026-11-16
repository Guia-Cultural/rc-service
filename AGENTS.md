# Rota Cultural — Backend (`rc-service`)

## Stack e Estrutura
- **Stack:** Java 25, Spring Boot 4.1.1, Spring Security + Nimbus JWT (HS256), Spring Data JPA, MySQL 8.0, Azure Storage Blob SDK, Maven Wrapper (`mvnw.cmd`/`mvnw`).
- **Pastas (`src/main/java/com/rotacultural/rcservice/`):**
  - `config/` (`SecurityConfig`, `JwtConfig`, `DataInitializer`, `GlobalExceptionHandler`)
  - `controllers/` (`AuthController`, `PlacesController`, `TripController`, `ItineraryController`, `RecommendationsController`, `FavoritesController`, `FeedbacksController`, `UserController`, `CategoryController`, `MediaController`, `HealthController`)
  - `services/` e `services/storage/` (`StorageService`, `FileSystemStorageService`, `AzureBlobStorageService`)
  - `entities/`, `repositories/`, `dtos/`
- **Configuração e SQL:** `src/main/resources/application*.properties` e `src/main/resources/db/migration/`.

## Padrões e Limitações
- Seguir MVC direto (`Controller -> Service -> Repository -> Entity/DTO`).
- Hierarquia: `ADMIN > NATIVO > USUARIO` (+ `VISITANTE` anônimo). Checar propriedade nos services (`authService.requireOwnerOrAdmin(...)`).
- Locais criados por `NATIVO` iniciam sempre como `EM_AVALIACAO`; consultas públicas retornam apenas `APROVADO`.
- Armazenamento via interface `StorageService` (local ou `azure` com `DefaultAzureCredential`); nunca salvar Base64 no MySQL nem URLs SAS expiradas no banco.

## Segurança por Ambiente
- Profiles: `dev` (local, fallback JWT de desenvolvimento e seed demo) e `prod` (exige `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET_BASE64` forte, `ddl-auto=validate`, seed demo desativado e `APP_STORAGE_PROVIDER=azure`).

## Comandos e Documentação
- Testes (perfil `test` em `rotacultural_test`): `.\mvnw.cmd test`
- Build JAR: `.\mvnw.cmd -DskipTests package`
- Produção: Flyway V1/V2 + `ddl-auto=validate`; JDBC exige `sslMode=VERIFY_IDENTITY`. Scripts versionados em `deploy/` usam Jib 3.5.2 e recursos existentes em `chilecentral`.
- Azure Express: identidade `id-rc-service` atribuída pelo usuário e `AZURE_CLIENT_ID`; reinício por ARM Stop/Start (revision restart não é suportado).
- Docs e progresso: `README.md`, `docs/MVP_PROGRESS.md`, skill `.agents/skills/rotacultural-backend/SKILL.md`.
- Git: commitar apenas em `feat/mvp-integration`; nunca fazer merge na `main` sem aprovação.

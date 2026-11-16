# Rota Cultural — Backend (`rc-service`)

API REST do projeto **Rota Cultural**, desenvolvida em **Java 25**, **Spring Boot 4.1.1**, **Spring Security**, **JWT (Nimbus OAuth2 Resource Server)**, **Spring Data JPA** e **MySQL 8.0**.

---

## Sumário

1. [Arquitetura e Tecnologias](#arquitetura-e-tecnologias)
2. [Pré-requisitos](#pré-requisitos)
3. [Configuração de Ambiente e Segurança](#configuração-de-ambiente-e-segurança)
4. [Banco de Dados, Migrations e Seed Inicial](#banco-de-dados-migrations-e-seed-inicial)
5. [Storage de Imagens (Local e Produção / Azure Blob)](#storage-de-imagens-local-e-produção--azure-blob)
6. [Como Executar Localmente](#como-executar-localmente)
7. [Testes Automatizados](#testes-automatizados)
8. [Docker](#docker)
9. [Papéis (Roles) e Regras de Autorização](#papéis-roles-e-regras-de-autorização)
10. [Principais Endpoints da API](#principais-endpoints-da-api)
11. [Status do MVP](#status-do-mvp)

---

## Arquitetura e Tecnologias

- **Linguagem / Runtime:** Java 25 (`eclipse-temurin:25`)
- **Framework:** Spring Boot `4.1.1`
- **Persistência:** Spring Data JPA / Hibernate + MySQL (`mysql-connector-j`)
- **Segurança:** Spring Security + `spring-boot-starter-security-oauth2-resource-server` (JWT HS256 assinado/validado com Nimbus `JwtEncoder` / `JwtDecoder`) + `BCryptPasswordEncoder`
- **Validação & Docs:** Jakarta Bean Validation + SpringDoc OpenAPI UI (`3.1.1` em `/swagger-ui.html`)
- **Storage:** Abstração `StorageService` com implementação local `FileSystemStorageService` (`multipart/form-data`)

---

## Pré-requisitos

- **JDK 25** configurado em `JAVA_HOME` (ou utilizado pelo Maven Wrapper `mvnw`/`mvnw.cmd`)
- **MySQL 8.0+** em execução na porta `3306`
- **Docker** (opcional, para build/execução em container)

---

## Configuração de Ambiente e Segurança

1. Copie `.env.example` para `.env` na raiz de `rc-service`:

```bash
cp .env.example .env
```

2. Preencha as variáveis conforme seu ambiente (**nunca versione `.env` nem chaves reais no Git**):

| Variável | Descrição | Valor padrão (`dev`) / Produção (`prod`) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Profile Spring ativo | `dev` (padrão local) / `prod` (obrigatório na Azure) |
| `DB_URL` | URL JDBC do MySQL 8.0 | Local `jdbc:mysql://localhost:3306/rotacultural...` / Obrigatório em `prod` |
| `DB_USERNAME` | Usuário do MySQL | Obrigatório em `prod` |
| `DB_PASSWORD` | Senha do MySQL | Obrigatório em `prod` |
| `JWT_SECRET_BASE64` | Segredo HMAC SHA-256 em Base64 (>= 256 bits) | Fallback **apenas em `dev`/`test`**; rejeitado em `prod` se vazio ou padrão |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas no CORS (separadas por vírgula) | `http://localhost:5173,...` / URL HTTPS da Vercel em `prod` |
| `APP_STORAGE_PROVIDER` | Provedor de mídia (`local` ou `azure`) | `local` em `dev` / `azure` em `prod` |
| `UPLOAD_DIR` | Diretório físico para uploads locais | `./uploads` |
| `AZURE_STORAGE_ACCOUNT_NAME` | Nome da Storage Account no Azure | Obrigatório quando `APP_STORAGE_PROVIDER=azure` |
| `AZURE_STORAGE_CONTAINER_NAME` | Container privado do Blob | `places-media` |
| `APP_BOOTSTRAP_ADMIN_EMAIL` / `PASSWORD` | Opcional para criar ADMIN inicial em `prod` | Vazio por padrão (nunca semeia `Senha@123` em `prod`) |

### Autenticação JWT (`JwtConfig`, `AuthService`, `AuthController`)

- `POST /auth/login` valida e-mail e senha via `PasswordEncoder` (BCrypt) e emite JWT HS256 com:
  - emissor (`iss`): `rc-service`
  - expiração (`exp`): `86400` segundos (24h)
  - claims: `sub` (ID do usuário), `userId`, `email`, `name`, `role`
  - permissões consultam o perfil atual no banco; rebaixamento/exclusão tem efeito imediato.
- `JwtConfig` rejeita em `prod` qualquer chave vazia ou igual ao segredo padrão de desenvolvimento.
- `GET /auth/me` e todos os endpoints protegidos recuperam o usuário autenticado exclusivamente a partir do token validado pelo `JwtDecoder` — nenhum `userId` enviado no corpo da requisição redefine propriedade de dados.

---

## Banco de Dados, Migrations e Seed Inicial

- Scripts SQL compatíveis com **MySQL 8.0**:
  - `src/main/resources/db/migration/V1__baseline_schema.sql`
  - `src/main/resources/db/migration/V2__mvp_evolution.sql`
- **Desenvolvimento (`dev`):** usa `application-dev.properties` (`ddl-auto=update`), seed idempotente de categorias, 3 usuários demo (`admin@rotacultural.com`, `nativo@rotacultural.com`, `usuario@rotacultural.com`, senha `Senha@123`), 15 locais (`12` `APROVADO`, `2` `EM_AVALIACAO`, `1` `REJEITADO`) e avaliações iniciais.
- **Produção (`prod`):** Flyway registra V1/V2 uma única vez, valida checksums e interrompe startup em erro; `ddl-auto=validate`, SQL init desativado e nenhum baseline/clean automático. `ProductionSafetyConfig` rejeita filesystem, seed demo, DDL automático e JDBC sem `sslMode=VERIFY_IDENTITY`.
- Antes do primeiro deploy, aplicar migrações com `deploy/DatabaseSetup.java migrate` e credenciais administrativas apenas no ambiente do processo. O mesmo utilitário (`provision-user`) cria `rc_app` com SELECT/INSERT/UPDATE/DELETE no schema; não redefine senha existente. Schema preexistente sem histórico exige auditoria e adoção manual, nunca baseline automático.

---

## Storage de Imagens (Local e Azure Blob Storage)

- Upload via `POST /places/{id}/images` (`multipart/form-data`, campo `files`, parâmetro opcional `cover=true|false`), com validação completa em `ImageFileValidator`:
  - limite de `5 MB` por arquivo (`25 MB` por requisição) e até `10` imagens por local;
  - formatos aceitos: `.jpg`, `.jpeg`, `.png`, `.webp` validados por extensão, `Content-Type` e assinatura binária (magic bytes);
  - rollback automático em falha parcial no lote para evitar arquivos órfãos;
  - apenas chave canônica `/uploads/places/{id}/{uuid}.{ext}` e metadados vão ao MySQL (sem Base64 nem URLs SAS expiradas gravadas no banco).
- Provedores sob a interface `StorageService`:
  - **`FileSystemStorageService` (`app.storage.provider=local`):** padrão em `dev`/`test`, gravando em `UPLOAD_DIR`.
  - **`AzureBlobStorageService` (`app.storage.provider=azure`):** padrão em `prod`, utilizando o SDK oficial `com.azure:azure-storage-blob` + `DefaultAzureCredential` (Managed Identity na Azure) em container **privado** (`places-media`).
- **Controle de acesso na leitura (`MediaController`):**
  - fotos de locais `APROVADO` são servidas publicamente via `GET /uploads/places/{placeId}/{filename}`;
  - fotos de locais `EM_AVALIACAO` ou `REJEITADO` exigem assinatura HMAC temporária na resposta da API ou autenticação do criador/`ADMIN` (`403 Forbidden` para anônimos sem assinatura).

---

## Como Executar Localmente

No PowerShell (Windows), carregando as variáveis do `.env` e garantindo JDK 25:

```powershell
cd rc-service
Get-Content .env | ForEach-Object {
  $line = $_.Trim()
  if ($line -and -not $line.StartsWith("#")) {
    $k, $v = $line.Split("=", 2)
    [Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), "Process")
  }
}
.\mvnw.cmd spring-boot:run
```

Ou empacotando e executando o JAR:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\rc-service-0.0.1-SNAPSHOT.jar
```

A API sobe em `http://localhost:8081` e a documentação OpenAPI fica disponível em `http://localhost:8081/swagger-ui.html`.

---

## Testes Automatizados

O perfil de teste (`src/test/resources/application-test.properties`) utiliza um schema MySQL isolado (`rotacultural_test` com `ddl-auto=create-drop`) para não interferir no banco de desenvolvimento.

```powershell
cd rc-service
.\mvnw.cmd test
```

Suites incluídas (`12` testes ao todo):
- `RcServiceApplicationTests`: carregamento do contexto Spring Boot + JPA + Security.
- `MigrationIntegrationTests`: MySQL em `rotacultural_migration_test`, V1/V2, validação Hibernate e reexecução sem perda de dados; `UploadSafetyTests` e `ProductionSafetyTests`: falhas de upload e configuração insegura.
- `MvpIntegrationTests`: cadastro com bloqueio de `ADMIN`/solicitação segura de `NATIVO`, login JWT e expiração, hierarquia e checagem de propriedade entre usuários, ciclo completo de locais + imagens protegidas enquanto `EM_AVALIACAO` + rejeição + aprovação, regra de 30 dias em viagens + recomendações determinísticas + roteiros + favoritos + avaliações, e validações de `/health`, guardião de chave JWT em `prod` e `AzureBlobStorageService`.

---

## Docker

O `Dockerfile` usa build multi-stage (`eclipse-temurin:25-jdk` para compilar e `eclipse-temurin:25-jre-alpine` no runtime, com usuário não-root `app` e diretório `/app/uploads` com permissões adequadas):

```bash
cd rc-service
docker build -t rc-service:mvp .
docker run --rm -p 8081:8081 --env-file .env rc-service:mvp
```

*(Nota de ambiente: certifique-se de que o daemon Docker esteja em execução antes de executar os comandos acima.)*

---

## Papéis (Roles) e Regras de Autorização

Hierarquia configurada no `SecurityConfig`: **`ADMIN > NATIVO > USUARIO`** (+ acesso anônimo **`VISITANTE`**).

- **VISITANTE:** consulta locais `APROVADO`, categorias e prévia de recomendações (`POST /recommendations/preview`), realiza cadastro (`POST /user`) e login (`POST /auth/login`).
- **USUARIO:** herda visitante; gerencia seus dados (`PUT /user/{id}`), favoritos, avaliações (1 por local, editável), viagens (máx. 30 dias), preferências e roteiros gerados.
- **NATIVO:** herda usuário; cadastra locais (`POST /places` inicia sempre como `EM_AVALIACAO`), envia/remove imagens dos próprios locais e consulta seus envios (`GET /places/my`).
- **ADMIN:** herda nativo; gerencia usuários e aprova solicitações de `NATIVO` (`PATCH/PUT /user/{id}/role`), gerencia categorias e aprova/rejeita locais (`PATCH/PUT /places/{id}/status`).

---

## Principais Endpoints da API

| Domínio | Endpoints principais |
|---|---|
| **Health & Mídia** | `GET /health`, `GET /uploads/places/{placeId}/{filename}` |
| **Auth** | `POST /auth/login`, `GET /auth/me` |
| **Usuários** | `POST /user`, `GET /user` (ADMIN), `GET /user/{id}`, `PUT /user/{id}`, `PATCH/PUT /user/{id}/role` (ADMIN), `DELETE /user/{id}` |
| **Categorias** | `GET /categories`, `GET /categories/{id}`, `POST /categories` (ADMIN), `PUT /categories/{id}` (ADMIN), `DELETE /categories/{id}` (ADMIN) |
| **Locais** | `GET /places` (`q`, `city`, `state`, `catId`, `price`), `GET /places/paged`, `GET /places/{id}`, `GET /places/my` (NATIVO), `GET /places/admin` (ADMIN), `POST /places` (NATIVO), `PUT /places/{id}`, `PATCH/PUT /places/{id}/status` (ADMIN), `DELETE /places/{id}` |
| **Imagens** | `POST /places/{id}/images`, `GET /places/{id}/images`, `DELETE /places/{placeId}/images/{imageId}` |
| **Viagens & Pref.** | `POST /trip`, `GET /trip/me`, `GET /trip/{id}`, `PUT /trip/{id}`, `DELETE /trip/{id}`, `GET /preferences/trip/{tripId}`, `POST/PUT/DELETE /preferences` |
| **Recomendações** | `POST /recommendations/preview`, `GET /recommendations/trip/{tripId}` |
| **Roteiros** | `POST /itineraries/generate/{tripId}`, `GET /itineraries/trip/{tripId}`, `GET /itineraries/me`, `GET/PUT/DELETE /itineraries/{id}` |
| **Favoritos** | `POST /favorites`, `GET /favorites/me`, `DELETE /favorites/place/{locId}` |
| **Avaliações** | `GET /feedbacks/place/{locId}`, `POST /feedbacks`, `PUT /feedbacks/{id}`, `DELETE /feedbacks/{id}` |

---

## Status do MVP

- **Concluído no código:** Auth JWT/BCrypt, permissões e propriedade, perfis dev/prod seguros, Flyway V1/V2 + validate, Blob privado/URLs assinadas, locais, viagens, preferências, recomendações determinísticas, roteiros, favoritos e avaliações. Suíte completa: 12/12.
- **Publicado:** Azure Students, `chilecentral`, imagem por digest e identidade `id-rc-service` atribuída pelo usuário (compatível com o ambiente Express). Scripts versionados em `deploy/`; estado e evidências em `docs/MVP_PROGRESS.md`.

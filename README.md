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

| Variável | Descrição | Valor padrão / exemplo |
|---|---|---|
| `DB_URL` | URL JDBC do MySQL | `jdbc:mysql://localhost:3306/rotacultural?createDatabaseIfNotExist=true` |
| `DB_USERNAME` | Usuário do MySQL | `SEU_USUARIO` |
| `DB_PASSWORD` | Senha do MySQL | `SUA_SENHA` |
| `JWT_SECRET_BASE64` | Segredo HMAC SHA-256 em Base64 (>= 256 bits) | Ver `.env.example` (substituir em produção) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas no CORS (separadas por vírgula) | `http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000` |
| `UPLOAD_DIR` | Diretório físico para uploads locais | `./uploads` |

### Autenticação JWT (`JwtConfig`, `AuthService`, `AuthController`)

- `POST /auth/login` valida e-mail e senha via `PasswordEncoder` (BCrypt) e emite JWT HS256 com:
  - emissor (`iss`): `rc-service`
  - expiração (`exp`): `86400` segundos (24h)
  - claims: `sub` (e-mail), `userId`, `email`, `name`, `role`
- `GET /auth/me` e todos os endpoints protegidos recuperam o usuário autenticado exclusivamente a partir do token validado pelo `JwtDecoder` — nenhum `userId` enviado no corpo da requisição redefine propriedade de dados.

---

## Banco de Dados, Migrations e Seed Inicial

- As definições SQL versionadas de referência estão em:
  - `src/main/resources/db/migration/V1__baseline_schema.sql`
  - `src/main/resources/db/migration/V2__mvp_evolution.sql`
- Em desenvolvimento local, o banco `rotacultural` é criado automaticamente (`createDatabaseIfNotExist=true`) e atualizado conforme configuração em `src/main/resources/application.properties`.
- O componente `DataInitializer` executa um seed **idempotente** na inicialização quando os registros ainda não existem:
  - **Usuários demonstrativos (senha padrão `Senha@123`):**
    - `admin@rotacultural.com` (`ADMIN`)
    - `nativo@rotacultural.com` (`NATIVO`)
    - `usuario@rotacultural.com` (`USUARIO`)
  - **Categorias:** 7 categorias culturais iniciais.
  - **Locais:** 15 locais (`12` `APROVADO`, `2` `EM_AVALIACAO`, `1` `REJEITADO`) e avaliações iniciais.

---

## Storage de Imagens (Local e Produção / Azure Blob)

- Upload via `POST /places/{id}/images` (`multipart/form-data`, campo `files`, parâmetro opcional `cover=true|false`).
- Regras aplicadas em `FileSystemStorageService`:
  - limite de `5 MB` por arquivo (`25 MB` por requisição configurado em `application.properties`);
  - formatos aceitos: `.jpg`, `.jpeg`, `.png`, `.webp` validados por extensão, `Content-Type` e assinatura binária (magic bytes);
  - nenhum arquivo é armazenado como Base64 no MySQL (apenas metadados e URL na tabela `place_images`).
- Em desenvolvimento local, os arquivos são gravados em `UPLOAD_DIR` (`./uploads`) e expostos em `/uploads/**` via `WebConfig`.

### O que falta configurar para produção (Azure Blob Storage)

A interface `com.rotacultural.rcservice.services.storage.StorageService` (`store(MultipartFile file, String folder)` e `delete(String fileUrl)`) já isola os services da infraestrutura física. Para ativar **Azure Blob Storage** em produção:

1. Adicionar a dependência oficial no `pom.xml` (ex.: `com.azure:azure-storage-blob` ou `com.azure.spring:spring-cloud-azure-starter-storage-blob`).
2. Implementar uma classe `@Service` (ex.: `AzureBlobStorageService`) sob `@Profile("prod")` ou `@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")`, marcando `FileSystemStorageService` para `local`/padrão.
3. Configurar no ambiente de produção as credenciais do Storage Account (não versionadas no Git):
   - `AZURE_STORAGE_CONNECTION_STRING` (ou `AZURE_STORAGE_ACCOUNT_NAME` + Managed Identity)
   - `AZURE_STORAGE_CONTAINER_NAME` (ex.: `places-media`)
   - `APP_STORAGE_PUBLIC_BASE_URL` (endpoint do Blob/CDN, caso o acesso não use SAS URL)

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

Suites incluídas:
- `RcServiceApplicationTests`: carregamento do contexto Spring Boot + JPA + Security.
- `MvpIntegrationTests`: testes de integração reais cobrindo cadastro com bloqueio de `ADMIN`/solicitação segura de `NATIVO`, login JWT e hierarquia de papéis, fluxo de aprovação de locais + upload/validação de imagens, regra de 30 dias em viagens + recomendações determinísticas + geração de roteiros + bloqueio entre usuários, e favoritos + regra de 1 avaliação editável por usuário/local.

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

- **Concluído:** Autenticação JWT completa + BCrypt, hierarquia `ADMIN > NATIVO > USUARIO`, solicitação segura de `NATIVO`, CRUD completo e aprovação de locais, upload/serviço multipart de imagens em filesystem local, busca/filtros/paginação de locais aprovados, viagens (limite 30 dias), preferências, recomendações determinísticas com banco MySQL, geração/persistência de roteiros por dia, favoritos e avaliações (1 por usuário/local com edição), migrations SQL (`V1`/`V2`), seed idempotente e testes de integração.
- **Pendente para Produção:** Implementação do provider **Azure Blob Storage** para `StorageService`, secrets gerenciados em cofre/provedor cloud e pipeline Flyway ativada caso opte por desligar `hibernate.ddl-auto`.

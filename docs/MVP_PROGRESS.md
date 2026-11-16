# MVP Progress — `rc-service`

- **Branch:** `feat/mvp-integration`
- **Concluído:** Auth JWT + hierarquia `ADMIN > NATIVO > USUARIO`, profiles `dev`/`prod` (`JwtConfig` bloqueia fallback em `prod`, `DataInitializer` não semeia contas/locais demo em `prod`), `V2__mvp_evolution.sql` compatível com MySQL 8.0, `AzureBlobStorageService` (`DefaultAzureCredential` + container privado) e `FileSystemStorageService`, `MediaController` com proteção para locais não aprovados, `GET /health`, `MvpIntegrationTests` (`7/7`), validação limpa de `V1`+`V2` com `ddl-auto=validate`.
- **Bloqueio local:** Docker Desktop daemon desligado na estação (`az acr build` previsto no script de deploy).
- **Próximo passo:** Aguardar aprovação do plano Azure/Vercel (Etapa 9) e publicar imagem/banco/storage (Etapa 10).

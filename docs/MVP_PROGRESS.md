# MVP Progress — rc-service

- Branch: `feat/mvp-integration`; origem `7222cc9` confirmada no GitHub em 16/11/2026.
- CONCLUÍDO: auditoria e correções de produção: Flyway V1/V2 com histórico/checksum, `validate`, guardas para TLS/Azure/seed, revogação imediata de perfil, extensão de upload e limpeza após rollback/falha Blob.
- CONCLUÍDO: `mvnw clean verify` — 12 testes, 0 falhas/erros; JDK 26.0.2 com release 25, bytecode major 69.
- CONCLUÍDO: MySQL Azure com certificado/hostname verificados, V1/V2 aplicadas no schema vazio, usuário rc_app com DML; container places-media privado.
- CONCLUÍDO: Jib 3.5.2 sem Docker; ACR confirmou `sha256:d388b1d39be6075ee341dead8fad2f4a344e4e8dec28815a933a439c6de4d60a` (Linux amd64, usuário 10001).
- EM ANDAMENTO: Container App Consumption autorizado na assinatura Students, RBAC, startup e jornadas reais. Scripts versionados em deploy/; segredos somente fora do Git/ARM em memória.

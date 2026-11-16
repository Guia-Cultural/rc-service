# MVP Progress — rc-service

- Branch feat/mvp-integration; commits de origem 7222cc9 e correções 4f25398/8756d3a confirmados e enviados.
- CONCLUÍDO: Maven clean verify, 12 testes sem falhas/erros; JDK 26.0.2 com bytecode Java 25 (major 69).
- CONCLUÍDO: Flyway V1/V2, schema validate, proteção prod TLS/Azure/sem demo; permissões atuais do banco prevalecem sobre roles antigas de JWT; uploads rejeitam extensão proibida e limpam após rollback/falha Blob.
- CONCLUÍDO: Azure Students/chilecentral; API https://rc-service-api.agreeableflower-26a5f1b0.chilecentral.azurecontainerapps.io, Running/Succeeded e /health UP/azure; 0,5 CPU/1Gi, 0–1 réplica.
- CONCLUÍDO: imagem acrrotacultural26.azurecr.io/rc-service@sha256:d388b1d39be6075ee341dead8fad2f4a344e4e8dec28815a933a439c6de4d60a (Linux amd64, não root). ACR admin desativado.
- CONCLUÍDO: MySQL TLS VERIFY_IDENTITY, V1/V2 e 9 tabelas de domínio + histórico; rc_app DML. Blob places-media privado; id-rc-service atribuída pelo usuário com AcrPull e Blob Contributor restrito ao container.
- CONCLUÍDO: jornadas API de usuário/NATIVO/ADMIN e segurança; persistência após ARM Stop/Start (Express não aceita revision restart). Fixtures QA Auditoria e120bf0d, local 1, rejeitado 2, viagem 1; senhas aleatórias fora do Git.
- PARCIAL: jornadas visuais; Computer Use bloqueou o navegador por não conseguir identificar URL Windows com confiança. Nenhum contorno.
- Risco residual: firewall MySQL aceita serviços Azure de outras assinaturas; TLS/DML ativos, acesso /32 temporário removido. Egress estável restrito depende de avaliação de rede, sem recursos pagos extras criados.
- Scripts versionados em deploy/. Express exige identidade atribuída + AZURE_CLIENT_ID. Segredos fora do Git e somente em memória/Container App Secrets.

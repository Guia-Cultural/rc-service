# CI/CD Azure e repositório privado — Rota Cultural

Este guia prepara `Guia-Cultural/rc-service` para funcionar como repositório **privado**, com testes em pushes/PRs e publicação no Azure Container Apps **somente após ativação explícita**. Nenhuma mudança de visibilidade ou provisionamento na Azure é feita pelo workflow.

## Estado inicial e ativação

O workflow `.github/workflows/azure-ci-cd.yml` executa `./mvnw clean verify` com MySQL 8 isolado em pushes na `main`, pull requests para a `main` e execução manual. **O job de deploy fica desabilitado** até definir `ENABLE_AZURE_DEPLOY=true` nas variáveis de Actions do repositório. Mesmo ativado, exige que o job de testes tenha passado.

**Antes de habilitar o deploy automático:**

1. Integre e teste o PR na `main`. A `main` deve conter a versão completa de produção (o conjunto de PRs #9, #10 e #11, conforme revisão do grupo). Deploy de uma `main` incompleta pode regredir a API.
2. No GitHub, em `Settings → Environments`, crie o ambiente `production` e configure *required reviewers* e restrição de branch `main`, conforme os recursos disponíveis no plano. Com aprovação obrigatória, um push não substitui a produção sem revisão.
3. Na Azure, crie ou utilize um principal de serviço com autenticação **OIDC federada** para GitHub Actions. O assunto federado do ambiente é `repo:Guia-Cultural/rc-service:environment:production`; audience `api://AzureADTokenExchange`. Use o tenant correto. Não armazene client secrets.
4. Conceda permissões mínimas necessárias ao principal OIDC: push no ACR `acrrotacultural26` (por exemplo, `AcrPush` para registry em modo RBAC apropriado) e atualização da Container App existente `rc-service-api` no grupo `rg-rotacultural` (por exemplo, role compatível de Container Apps restrita ao recurso). Confirme o modo de permissões do ACR e a possibilidade de leitura de resource group/Container Apps environment, sem conceder acesso geral a recursos não relacionados. Não altere a identidade `id-rc-service`, que é usada em tempo de execução.
5. Em `Settings → Secrets and variables → Actions → Variables`, configure `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` (a assinatura deve ser `de7d7912-a0cf-438f-9a3a-e7c985dc794a`). Esses valores são identificadores, não senhas.
6. **Antes** de definir `ENABLE_AZURE_DEPLOY=true`, valide em ambiente autorizado se `az containerapp update --image` funciona com o workload profile Express/Consumption atual **sem modificar secrets, CORS, identidades, probes, ingress ou escala**. A implantação anterior do projeto utilizou ARM PUT especificamente devido a limitações da CLI em Express. Se o comando não atender aos requisitos, adapte o workflow para uma atualização mínima verificada e revisada; não execute o ARM PUT de `deploy/azure-deploy.ps1` diretamente no CI, pois ele recompõe segredos de produção.
7. Habilite `ENABLE_AZURE_DEPLOY=true` e confirme uma execução com aprovação manual. O workflow compila a imagem com Maven/Jib e ACR por digest, atualiza a **imagem** do aplicativo já existente e verifica o FQDN e `/health`. Não cria App, ACR, MySQL, Storage ou identidade.
8. Ao encerrar uma janela de publicação ou durante incidentes, desabilite o deploy removendo a variável ou atribuindo `false`.

## Repositório privado

A conexão GitHub utilizada para preparar esta mudança **não expõe uma ação de alteração de visibilidade**. Depois de revisar a automação, entre em `https://github.com/Guia-Cultural/rc-service/settings`, abra `Danger Zone → Change repository visibility → Make private` e confirme como administrador da organização. Verifique as permissões do GitHub Actions na organização e eventuais restrições dos colaboradores.

**Tornar `rc-service` privado não apaga a imagem já presente no ACR nem derruba o Container Apps.** GitHub Actions pode executar no repositório privado, sujeito à política de Actions, faturamento/cotas e permissões da organização. A publicação futura depende do OIDC, das funções RBAC e do funcionamento do comando de atualização de imagem. A Vercel usa o repositório distinto `rc-web`, que não é alterado aqui.

## Diagnóstico

- Testes falhando: examine MySQL 8, JDK 25, o profile `migration` e Flyway; o banco do runner é isolado.
- OIDC falhando: confira `repo:Guia-Cultural/rc-service:environment:production`, tenant, audience e restrições da identidade federada.
- ACR negando push: confira o modo de autorização do registry e permissões de push do principal OIDC.
- `az containerapp update` falhando: **não execute deploy forçado nem recrie recursos**; investigue limitações Express e documente correção.
- `/health` falhando: verifique a revisão, logs e o consumo de memória; não altere configurações ou segredos automaticamente.
- `ENABLE_AZURE_DEPLOY` ausente/false: testes rodam, deploy é ignorado (*skipped*).

Nenhuma credencial da base de produção ou JWT é necessária ao job de build. Os segredos de runtime existentes devem permanecer sob gerenciamento atual do Container Apps.

**Esta configuração não comprova deploy automático funcional até ser validada em execução autorizada.**

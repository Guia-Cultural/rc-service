# Consolidação do backend — 16/11/2026

**CONCLUÍDO:** branch `integration/mvp-consolidado`, criada de `origin/main` em worktree isolado. Nenhum deploy ou alteração em produção foi executado nesta tarefa.

## Origens e histórico

| Referência revisada | SHA inicial |
|---|---|
| main / origin/main | `2b09425a32d6e45c1cbd0d42a6c1db86c362654a` |
| feat/complete-services-controllers | `54939a8bddceb1488d4f6103c7bfaf6ac800363f` |
| feat/mvp-integration | `4c88fabba5a1b9db9fb58db17201acbe7251fe0b` |

- `fetch --all` não encontrou alterações concorrentes. Checkouts originais limpos, sem conflitos, tags ou worktrees anteriores; `.env`/`target` ignorados e não copiados.
- `main` e `feat/complete-services-controllers` já eram ancestrais do MVP. Os 18 commits de `origin/main..origin/feat/mvp-integration` foram incorporados, sem squash, por `merge --no-ff`.
- Merge funcional: `7ec24d145846cbefdc32a6bfcc489eb6235228ba`. Sem conflitos. Sua árvore é idêntica à origem MVP.
- Commits MVP relevantes preservados: `0441def`, `7222cc9`, `4f25398`, `8756d3a`, `4c88fab`, além de todo o histórico anterior.
- SHA final da branch: resolver `git rev-parse integration/mvp-consolidado`; o SHA que inclui este relatório é informado na entrega, evitando referência circular ao próprio commit.
- SHA final esperado de main: `2b09425a32d6e45c1cbd0d42a6c1db86c362654a`, conferido novamente antes/depois do push. Nenhuma branch antiga será enviada ou removida.

## Preservação e diferenças

- AuthService, JwtConfig, SecurityConfig e autorização por proprietário; usuários, categorias, locais/moderação, imagens/MediaController, favoritos, avaliações, viagens, preferências, roteiros e recomendações: código e endpoints idênticos ao MVP de origem.
- AzureBlobStorageService, FileSystemStorageService, Flyway V1/V2, Docker/Jib, scripts Azure, testes, AGENTS.md e skills preservados sem alteração. Somente documentação de consolidação foi adicionada após o merge.
- Dois DTOs antigos de main em `DTOs/Favorites/FavResp.java` e `DTOs/Itineraries/ItineraryResp.java` já foram substituídos no MVP pelos homônimos em `dtos/favorites` e `dtos/itineraries`. Todos os campos antigos permanecem; respostas modernas acrescentam dados do local. Não há imports dos pacotes antigos. Demais renomeações de DTOs aparecem no diff Git.
- Nenhum fallback JWT prod, permissão ou validação de upload foi enfraquecido. Prod continua exigindo Blob Azure, schema validate, ausência de seed demo e JDBC VERIFY_IDENTITY; segredos obrigatórios, Blob privado e Managed Identity permanecem configurados no código.

## Validação desta branch

- **CONCLUÍDO:** Maven `clean verify dependency:tree`, 12 testes, 0 falhas/erros; JDK 26.0.2 com release 25, bytecode major 69. Inclui integração MySQL, Flyway/reexecução, JWT/roles/propriedade, mídia, viagens e upload.
- **CONCLUÍDO:** dependências resolvidas; Spring Boot 4.1.1, Flyway 12.4.0 + módulo MySQL, Connector/J 9.7.0, Azure Blob 12.29.0 e Identity 1.15.0 preservados. O aviso anterior de versões Netty/Azure não foi tratado como motivo para downgrade.
- **CONCLUÍDO:** profiles test/migration usam somente schemas locais `rotacultural_test`/`rotacultural_migration_test`; profile dev iniciou em loopback e schema local separado `rotacultural_consolidation_ui`, com `/health` UP/local e catálogo aprovado.
- **CONCLUÍDO:** comparação de código, endpoints, migrations, configurações, AGENTS e skills com `4c88fab` sem diferenças funcionais.
- **NÃO TESTADO:** novo deploy, startup prod desta branch e nova validação do Azure. Evidências de produção anteriores em MVP_PROGRESS.md não representam publicação da consolidação.

## Revisão e riscos

- PR #8 permanece OPEN/DRAFT, `feat/mvp-integration -> main`; sem merge, fechamento, retarget ou Ready for Review. Nenhum novo PR para main foi criado.
- Main permanece ancestral e o diff futuro é calculável. Worktree: `F:/rotaCultural/.worktrees/rc-service-consolidado`.
- Risco residual já conhecido: firewall MySQL permite serviços Azure de outras assinaturas. Nenhuma configuração Azure/MySQL/Blob/secret foi alterada nesta tarefa.
- Alterações visuais e jornadas completas de navegador dependem da validação do frontend; não se afirma ausência total de regressões.

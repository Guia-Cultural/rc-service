<# Atualiza somente o app no ambiente existente. Criação inicial exige -AllowCreate.
   Não provisiona banco, registry, storage, environment, SKUs nem região.
   Banco deve estar migrado (DatabaseSetup.java); runtime usa rc_app com DML.
   SecretsPath e RuntimeDbSecretPath devem apontar para arquivos FORA do Git.
#>
param(
    [Parameter(Mandatory=$true)][string]$Image,
    [Parameter(Mandatory=$true)][string]$CorsAllowedOrigins,
    [string]$SecretsPath,
    [string]$RuntimeDbSecretPath,
    [switch]$AllowCreate,
    [switch]$Apply
)
$ErrorActionPreference = 'Stop'
$env:AZURE_CORE_ONLY_SHOW_ERRORS = 'true'
$subscription = 'de7d7912-a0cf-438f-9a3a-e7c985dc794a'
$group = 'rg-rotacultural'
$appName = 'rc-service-api'
if ($Image -notmatch '^acrrotacultural26\.azurecr\.io/rc-service@sha256:[a-f0-9]{64}$') { throw 'Exige imagem por digest do ACR autorizado.' }
foreach ($origin in $CorsAllowedOrigins.Split(',')) {
    $uri = [Uri]$origin
    if ($uri.Scheme -ne 'https' -or $uri.GetLeftPart([UriPartial]::Authority) -ne $origin) { throw 'CORS exige origens HTTPS exatas.' }
}
$account = az account show -o json | ConvertFrom-Json
if ($LASTEXITCODE -ne 0 -or $account.id -ne $subscription) { throw 'Assinatura incorreta.' }
$environment = az containerapp env show -g $group -n cae-rotacultural -o json | ConvertFrom-Json
if ($LASTEXITCODE -ne 0 -or ($environment.location -replace '\s','').ToLowerInvariant() -ne 'chilecentral' -or $environment.properties.provisioningState -ne 'Succeeded') { throw 'Ambiente existente não está pronto em chilecentral.' }
if (!$Apply) { Write-Output "Plano: $appName, $Image, Consumption 0.5 CPU/1Gi, replicas 0-1, HTTPS 8081, identidade/RBAC, CORS $CorsAllowedOrigins"; return }
if (!$SecretsPath -or !$RuntimeDbSecretPath) { throw 'Apply exige os arquivos secretos externos para preservar os valores existentes.' }
foreach ($secretFile in @($SecretsPath, $RuntimeDbSecretPath)) {
    $resolved = (Resolve-Path -LiteralPath $secretFile).Path
    $repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
    if ($resolved.StartsWith($repository + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Secrets devem ficar fora do repositório.' }
}
$existing = az containerapp list -g $group --query "[?name=='$appName'].name" -o tsv
if ($LASTEXITCODE -ne 0) { throw 'Falha ao descobrir apps.' }
if (!$existing) {
    if (!$AllowCreate) { throw 'App ausente; criação exige autorização e -AllowCreate.' }
    # Bootstrap oficial: imagem pública sem ingresso, depois identidade -> AcrPull -> imagem privada.
    $ErrorActionPreference = 'Continue' # A CLI escreve progresso em stderr no Windows PowerShell 5.
    az containerapp create -g $group -n $appName --environment cae-rotacultural --workload-profile-name Consumption --image mcr.microsoft.com/k8se/quickstart:latest --system-assigned --cpu 0.5 --memory 1Gi --min-replicas 0 --max-replicas 1 --only-show-errors -o none
    $createResult = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($createResult -ne 0) { throw 'Falha no bootstrap do app.' }
}
$app = az containerapp show -g $group -n $appName -o json | ConvertFrom-Json
if ($LASTEXITCODE -ne 0 -or !$app.identity.principalId) { throw 'Identidade de sistema ausente.' }
$acrId = az acr show -g $group -n acrrotacultural26 --query id -o tsv
$storageId = az storage account show -g $group -n sarotacultural26 --query id -o tsv
$blobScope = "$storageId/blobServices/default/containers/places-media"
foreach ($binding in @(@{role='AcrPull';scope=$acrId}, @{role='Storage Blob Data Contributor';scope=$blobScope})) {
    az role assignment create --assignee-object-id $app.identity.principalId --assignee-principal-type ServicePrincipal --role $binding.role --scope $binding.scope --only-show-errors -o none
    if ($LASTEXITCODE -ne 0) { throw 'Falha no RBAC.' }
}
$configuration = @{activeRevisionsMode='Single'; registries=@(@{server='acrrotacultural26.azurecr.io';identity='system'}); ingress=@{external=$true;targetPort=8081;transport='auto';allowInsecure=$false;traffic=@(@{latestRevision=$true;weight=100})}}
$envVars = @(
    @{name='SPRING_PROFILES_ACTIVE';value='prod'},
    @{name='DB_URL';value='jdbc:mysql://mysql-rotacultural-26.mysql.database.azure.com:3306/rotacultural?sslMode=VERIFY_IDENTITY&serverTimezone=UTC'},
    @{name='DB_USERNAME';value='rc_app'},
    @{name='DB_PASSWORD';secretRef='db-password'},
    @{name='JWT_SECRET_BASE64';secretRef='jwt-secret'},
    @{name='CORS_ALLOWED_ORIGINS';value=$CorsAllowedOrigins},
    @{name='APP_STORAGE_PROVIDER';value='azure'},
    @{name='AZURE_STORAGE_ACCOUNT_NAME';value='sarotacultural26'},
    @{name='AZURE_STORAGE_CONTAINER_NAME';value='places-media'}
)
if ($SecretsPath -and $RuntimeDbSecretPath) {
    $secrets = Get-Content -Raw -LiteralPath $SecretsPath | ConvertFrom-Json
    $dbSecrets = Get-Content -Raw -LiteralPath $RuntimeDbSecretPath | ConvertFrom-Json
    if (!$secrets.jwtSecretBase64 -or !$dbSecrets.appDbPassword) { throw 'Secrets obrigatórios ausentes.' }
    $configuration.secrets = @(@{name='db-password';value=$dbSecrets.appDbPassword},@{name='jwt-secret';value=$secrets.jwtSecretBase64})
    if ($secrets.bootstrapAdminEmail -and $secrets.bootstrapAdminPassword) {
        $configuration.secrets += @{name='bootstrap-admin-password';value=$secrets.bootstrapAdminPassword}
        $envVars += @{name='APP_BOOTSTRAP_ADMIN_EMAIL';value=$secrets.bootstrapAdminEmail},@{name='APP_BOOTSTRAP_ADMIN_PASSWORD';secretRef='bootstrap-admin-password'}
    }
}
$probes = @(
    @{type='Startup';httpGet=@{path='/health';port=8081};periodSeconds=5;timeoutSeconds=5;failureThreshold=60},
    @{type='Readiness';httpGet=@{path='/health';port=8081};periodSeconds=10;timeoutSeconds=5;failureThreshold=6},
    @{type='Liveness';httpGet=@{path='/health';port=8081};initialDelaySeconds=60;periodSeconds=30;timeoutSeconds=5;failureThreshold=5}
)
$payload = @{location='chilecentral';identity=@{type='SystemAssigned'};properties=@{
    managedEnvironmentId=$environment.id;workloadProfileName='Consumption';configuration=$configuration;
    template=@{containers=@(@{name='rc-service';image=$Image;env=$envVars;resources=@{cpu=0.5;memory='1Gi'};probes=$probes});scale=@{minReplicas=0;maxReplicas=1;rules=@(@{name='http';http=@{metadata=@{concurrentRequests='20'}}})}}
}}
# ARM em memória: senhas não entram em arquivos temporários, argumentos nem stdout.
$accessToken = az account get-access-token --resource https://management.azure.com --query accessToken -o tsv
if ($LASTEXITCODE -ne 0) { throw 'Falha ao autenticar no ARM.' }
$url = "https://management.azure.com/subscriptions/$subscription/resourceGroups/$group/providers/Microsoft.App/containerApps/${appName}?api-version=2025-01-01"
try {
    $null = Invoke-RestMethod -Method Put -Uri $url -Headers @{Authorization="Bearer $accessToken"} -ContentType 'application/json' -Body ($payload | ConvertTo-Json -Depth 30 -Compress)
} catch { throw 'ARM recusou atualização; consultar estado e logs sem imprimir o payload secreto.' }
az acr update -g $group -n acrrotacultural26 --admin-enabled false --only-show-errors -o none
if ($LASTEXITCODE -ne 0) { throw 'Falha ao desativar login administrador do ACR.' }
Write-Output 'Atualização solicitada. Verifique revision, logs e /health antes de declarar publicação concluída.'

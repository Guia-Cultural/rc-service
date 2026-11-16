param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$env:AZURE_CORE_ONLY_SHOW_ERRORS = 'true'
if (!$JavaHome) { throw 'Informe -JavaHome com JDK 25 ou superior.' }
$env:JAVA_HOME = $JavaHome
$env:PATH = "$JavaHome\bin;$env:PATH"
Push-Location (Join-Path $PSScriptRoot '..')
try {
    $account = az account show -o json | ConvertFrom-Json
    if ($LASTEXITCODE -ne 0 -or $account.id -ne 'de7d7912-a0cf-438f-9a3a-e7c985dc794a') {
        throw 'Selecione a assinatura Azure for Students do Rota Cultural.'
    }
    # Token transitório somente em memória/ambiente; nunca em argumentos do Maven.
    $auth = az acr login --name acrrotacultural26 --expose-token -o json | ConvertFrom-Json
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao autenticar no ACR.' }
    $env:RC_ACR_USERNAME = '00000000-0000-0000-0000-000000000000'
    $env:RC_ACR_TOKEN = $auth.accessToken
    $ErrorActionPreference = 'Continue'
    & .\mvnw.cmd -B -ntp -DskipTests compile jib:build
    $ErrorActionPreference = 'Stop'
    if ($LASTEXITCODE -ne 0) { throw 'Build/push Jib falhou.' }
    $digest = (Get-Content target/jib-image.digest -Raw).Trim()
    $remote = az acr manifest show-metadata --registry acrrotacultural26 --name 'rc-service:mvp' --query digest -o tsv
    if ($LASTEXITCODE -ne 0 -or $remote.Trim() -ne $digest) { throw 'Digest remoto diverge do build.' }
    Write-Output "Imagem verificada: acrrotacultural26.azurecr.io/rc-service@$digest"
} finally {
    Remove-Item Env:RC_ACR_USERNAME,Env:RC_ACR_TOKEN -ErrorAction SilentlyContinue
    Pop-Location
}

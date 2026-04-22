$ErrorActionPreference = "Stop"

Write-Host "1. Configurando Variáveis de Ambiente..."
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:NDK_ROOT = "$env:LOCALAPPDATA\Android\Sdk\ndk\23.1.7779620"
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"

if (-Not (Test-Path $env:NDK_ROOT)) {
    Write-Host "ERRO: O NDK 23.1.7779620 não está instalado no teu Android Studio." -ForegroundColor Red
    Write-Host "Vai ao Android Studio -> Tools -> SDK Manager -> SDK Tools -> NDK (Side by side) -> Instala a versão 23.1.7779620" -ForegroundColor Yellow
    exit 1
}

Write-Host "2. Instalando ferramentas Gomobile..."
go install golang.org/x/mobile/cmd/gomobile@latest
go install golang.org/x/mobile/cmd/gobind@latest

$GOPATH = go env GOPATH
$GOMOBILE = "$GOPATH\bin\gomobile.exe"

Write-Host "3. Gerando versão base (tailscale.version)..."
go run tailscale.com/cmd/mkversion > tailscale.version
$VERSION_LONG = (Select-String -Path "tailscale.version" -Pattern "VERSION_LONG=""(.*)""").Matches.Groups[1].Value
$VERSION_SHORT = (Select-String -Path "tailscale.version" -Pattern "VERSION_SHORT=""(.*)""").Matches.Groups[1].Value
$VERSION_GIT_HASH = (Select-String -Path "tailscale.version" -Pattern "VERSION_GIT_HASH=""(.*)""").Matches.Groups[1].Value

$LDFLAGS = "-X tailscale.com/version.longStamp=$VERSION_LONG -X tailscale.com/version.shortStamp=$VERSION_SHORT -X tailscale.com/version.gitCommitStamp=$VERSION_GIT_HASH -linkmode=external -extldflags=-Wl,-z,max-page-size=16384"
$TAGS = "not_tailscale_go ts_omit_cachenetmap"

Write-Host "4. Inicializando dependências Go..."
go mod tidy

$outDir = "android\libs"
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Force -Path $outDir }

Write-Host "5. Compilando Biblioteca Nativa libtailscale.aar (Pode levar alguns minutos)..."
& $GOMOBILE bind -target android -androidapi 26 -tags $TAGS -ldflags $LDFLAGS -o "$outDir\libtailscale.aar" ./libtailscale

Write-Host "COMPILADO COM SUCESSO! Agora podes abrir a pasta 'android' no teu Android Studio!" -ForegroundColor Green

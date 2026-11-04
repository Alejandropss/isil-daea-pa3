# Funciones compartidas por los casos de la PA3.
# Requisitos: MySQL (3306), WildFly (8082) y Spring Boot (8081) en ejecución.
# MYSQL_EXE: ruta de mysql.exe si no está en el PATH.
# Las claves de laboratorio se leen de variables de entorno o de application.properties; no se muestran.

[Console]::OutputEncoding = [Text.Encoding]::UTF8
$OutputEncoding = [Text.Encoding]::UTF8

$Base = 'http://localhost:8081/sistema-pedidos'
$Inventario = 'http://localhost:8082/inventario-service/api/inventario'
$Propiedades = Get-Content "$PSScriptRoot\..\..\sistema-pedidos-springboot\src\main\resources\application.properties" -Raw -Encoding UTF8
$MySqlExe = if ($env:MYSQL_EXE) { $env:MYSQL_EXE } else { 'mysql' }

try {
  $maximo = $Host.UI.RawUI.MaxPhysicalWindowSize
  $ancho = [Math]::Min(125, $maximo.Width)
  $Host.UI.RawUI.BufferSize = New-Object Management.Automation.Host.Size($ancho, 3000)
  $Host.UI.RawUI.WindowSize = New-Object Management.Automation.Host.Size($ancho, [Math]::Min(58, $maximo.Height - 1))
} catch { }

function ClaveLab([string]$variable, [string]$propiedad) {
  $valor = [Environment]::GetEnvironmentVariable($variable)
  if ($valor) { return $valor }
  return [regex]::Match($Propiedades, [regex]::Escape($propiedad) + '=\$\{[A-Z_]+:(.+?)\}').Groups[1].Value
}

function Titulo([string]$texto) {
  $Host.UI.RawUI.WindowTitle = $texto
  Write-Host ('=' * 100) -ForegroundColor Cyan
  Write-Host "  $texto" -ForegroundColor Cyan
  Write-Host ('=' * 100) -ForegroundColor Cyan
}

function Paso([string]$texto) {
  Write-Host ''
  Write-Host ">> $texto" -ForegroundColor Yellow
}

function Color([int]$codigo) {
  if ($codigo -ge 200 -and $codigo -lt 400) { return 'Green' }
  if ($codigo -ge 500) { return 'Magenta' }
  return 'Red'
}

# Inicia sesión con el formulario real de Spring Security y devuelve el archivo de cookies.
function Login([string]$usuario) {
  $clave = if ($usuario -eq 'operador') { ClaveLab 'PEDIDOS_OPERADOR_PASSWORD' 'demo.security.operador-password' }
           else { ClaveLab 'PEDIDOS_CONSULTA_PASSWORD' 'demo.security.consulta-password' }
  $cookies = [IO.Path]::GetTempFileName()
  $pagina = (& curl.exe -s -c $cookies -b $cookies "$Base/login") -join ''
  $token = [regex]::Match($pagina, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
  $codigo = & curl.exe -s -o NUL -w '%{http_code}' -c $cookies -b $cookies -X POST "$Base/login" `
    --data-urlencode "username=$usuario" --data-urlencode "password=$clave" --data-urlencode "_csrf=$token"
  Write-Host "   Sesión iniciada como $usuario (POST /login -> HTTP $codigo)" -ForegroundColor DarkGray
  return $cookies
}

# Token CSRF de la sesión, tomado de la pantalla de reportes.
function TokenCsrf([string]$cookies) {
  $pagina = (& curl.exe -s -c $cookies -b $cookies "$Base/reportes/pedidos") -join ''
  return [regex]::Match($pagina, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
}

function MostrarJson([string]$texto) {
  if (-not $texto) { return }
  try { $datos = $texto | ConvertFrom-Json } catch { return }
  $lista = @($datos)
  if ($lista.Count -eq 0) { Write-Host '      [] (lista vacía)' -ForegroundColor Gray; return }
  foreach ($o in $lista) {
    $props = $o.PSObject.Properties | Where-Object { $_.Name -notin @('fecha', 'productoCodigo', 'precioUnitario', 'path') }
    Write-Host ('      { ' + (($props | ForEach-Object { "$($_.Name): $($_.Value)" }) -join ', ') + ' }') -ForegroundColor Gray
  }
}

# GET con la sesión indicada (o anónimo). Muestra URL, código y JSON.
function Get([string]$ruta, [string]$cookies) {
  Write-Host "   GET $ruta" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  $url = if ($ruta -like 'http*') { $ruta } else { "$Base$ruta" }
  $argumentos = @('-s', '-g', '-o', $salida, '-w', '%{http_code} %{redirect_url}', '--max-time', '10')
  if ($cookies) { $argumentos += @('-b', $cookies) }
  $r = & curl.exe @argumentos $url
  $codigo, $destino = "$r".Split(' ', 2)
  if ([int]$codigo -eq 0) { Write-Host '   <- Sin respuesta: conexión rechazada (servicio detenido)' -ForegroundColor Magenta; return }
  $extra = if ($destino) { " -> redirige a $($destino.Replace('http://localhost:8081', ''))" } else { '' }
  Write-Host "   <- HTTP $codigo$extra" -ForegroundColor (Color ([int]$codigo))
  $texto = [IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8)
  if ($texto.TrimStart().StartsWith('[') -or $texto.TrimStart().StartsWith('{')) { MostrarJson $texto }
  else {
    $m = [regex]::Match($texto, 'class="(?:alerta )?error">\s*(.*?)\s*</div>', 'Singleline')
    if ($m.Success) { Write-Host "      Mensaje en la pantalla: $($m.Groups[1].Value)" -ForegroundColor Gray }
    elseif ($texto -match 'Reportes de pedidos') { Write-Host '      Se muestra la pantalla reportes-pedidos.jsp' -ForegroundColor Gray }
  }
}

# POST /pedidos con el formulario. $token vacío = sin CSRF.
function RegistrarPedido([string]$cookies, [string]$token, [string]$cliente, [string]$codigo, [int]$cantidad) {
  $conToken = if ($token) { 'con CSRF' } else { 'SIN CSRF' }
  Write-Host "   POST /pedidos ($conToken): cliente=$cliente, productoCodigo=$codigo, cantidad=$cantidad" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  $argumentos = @('-s', '-o', $salida, '-w', '%{http_code} %{redirect_url}', '--max-time', '10', '-b', $cookies, '-X', 'POST', "$Base/pedidos",
    '--data-urlencode', "cliente=$cliente", '--data-urlencode', "productoCodigo=$codigo", '--data-urlencode', "cantidad=$cantidad")
  if ($token) { $argumentos += @('--data-urlencode', "_csrf=$token") }
  $r = & curl.exe @argumentos
  $http, $destino = "$r".Split(' ', 2)
  if ([int]$http -eq 302) { Write-Host "   <- HTTP 302 -> $($destino.Replace('http://localhost:8081', ''))  (pedido registrado)" -ForegroundColor Green; return }
  Write-Host "   <- HTTP $http" -ForegroundColor (Color ([int]$http))
  $html = [IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8)
  $m = [regex]::Match($html, 'class="error">\s*(.*?)\s*</div>', 'Singleline')
  if ($m.Success) { Write-Host "      Mensaje en la pantalla: $($m.Groups[1].Value)" -ForegroundColor Gray }
}

# Consulta directa a MySQL para comparar con lo que responde la aplicación.
function MySql([string]$sql) {
  Write-Host "   [MySQL] $sql" -ForegroundColor DarkGray
  $env:MYSQL_PWD = ClaveLab 'DB_PASSWORD' 'spring.datasource.password'
  $filas = & $MySqlExe -h 127.0.0.1 -u spedidos_app -D pedidosdb --default-character-set=utf8mb4 -t -e $sql
  Remove-Item Env:\MYSQL_PWD
  if ($filas) { $filas | ForEach-Object { Write-Host "      $_" -ForegroundColor Green } } else { Write-Host '      (sin filas)' -ForegroundColor Green }
}

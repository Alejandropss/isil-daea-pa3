. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 4 - Inventario detenido: los reportes siguen funcionando'

$operador = Login 'operador'
$token = TokenCsrf $operador

Paso 'Pedidos en MySQL antes del intento'
MySql 'SELECT COUNT(*) AS pedidos FROM pedido'

Paso 'Detener inventario-service (se finaliza WildFly en el puerto 8082)'
$conexion = Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conexion) {
  Stop-Process -Id $conexion.OwningProcess -Force
  Start-Sleep -Seconds 3
  Write-Host "   WildFly detenido (PID $($conexion.OwningProcess))." -ForegroundColor Magenta
}
Get "$Inventario/productos"

Paso 'Registrar un pedido: se informa la indisponibilidad de forma controlada'
RegistrarPedido $operador $token 'Luis Paz' 'LAP-001' 1

Paso 'Los reportes siguen funcionando porque solo consultan MySQL'
Get '/api/v1/pedidos/busqueda-avanzada?cliente=Ana&totalMinimo=1000' $operador
Get '/api/v1/pedidos/clientes-frecuentes?minPedidos=2' $operador
Get '/reportes/pedidos?cliente=Ana&totalMinimo=1000&minPedidos=2' $operador

Paso 'No se guardó el pedido fallido'
MySql 'SELECT COUNT(*) AS pedidos FROM pedido'

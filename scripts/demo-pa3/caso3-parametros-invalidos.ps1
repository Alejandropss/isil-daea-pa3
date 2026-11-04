. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 3 - Parámetros inválidos'

$consulta = Login 'consulta'

Paso 'Cliente vacío'
Get '/api/v1/pedidos/busqueda-avanzada?cliente=&totalMinimo=100' $consulta

Paso 'Total mínimo negativo'
Get '/api/v1/pedidos/busqueda-avanzada?cliente=Ana&totalMinimo=-1' $consulta

Paso 'minPedidos menor que 1'
Get '/api/v1/pedidos/clientes-frecuentes?minPedidos=0' $consulta

Paso 'Total mínimo que no es un número'
Get '/api/v1/pedidos/busqueda-avanzada?cliente=Ana&totalMinimo=abc' $consulta

Paso 'Pantalla JSP con minPedidos=0'
Get '/reportes/pedidos?minPedidos=0' $consulta

Paso "Texto SQL como cliente: viaja como parámetro y no trae pedidos (cliente=' OR '1'='1)"
Get "/api/v1/pedidos/busqueda-avanzada?cliente='%20OR%20'1'='1&totalMinimo=0" $consulta

Paso 'La tabla sigue intacta'
MySql 'SELECT COUNT(*) AS pedidos FROM pedido'

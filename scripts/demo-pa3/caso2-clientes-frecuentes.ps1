. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 2 - Clientes frecuentes y consolidado'

$consulta = Login 'consulta'

Paso 'Con minPedidos=2 solo aparece Ana Torres (Carlos Rojas tiene 1 pedido)'
Get '/api/v1/pedidos/clientes-frecuentes?minPedidos=2' $consulta

Paso 'Con minPedidos=1 aparecen los dos clientes'
Get '/api/v1/pedidos/clientes-frecuentes?minPedidos=1' $consulta

Paso 'Comprobación en MySQL: pedidos, unidades, importe y promedio por cliente'
MySql 'SELECT cliente, COUNT(*) AS pedidos, SUM(cantidad) AS unidades, SUM(total) AS importe, ROUND(AVG(total), 2) AS promedio FROM pedido GROUP BY cliente'

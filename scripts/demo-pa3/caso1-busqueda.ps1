. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 1 - Búsqueda por cliente y monto mínimo'

Paso 'Preparar datos: el operador registra tres pedidos (reservan stock en Inventario)'
$operador = Login 'operador'
$token = TokenCsrf $operador
RegistrarPedido $operador $token 'Ana Torres' 'LAP-001' 2
RegistrarPedido $operador $token 'Ana Torres' 'TEC-001' 1
RegistrarPedido $operador $token 'Carlos Rojas' 'MON-001' 1

Paso 'Todos los pedidos en MySQL'
MySql 'SELECT id, cliente, cantidad, total FROM pedido ORDER BY id DESC'

Paso 'API JSON como CONSULTA: cliente=Ana y totalMinimo=1000'
$consulta = Login 'consulta'
Get '/api/v1/pedidos/busqueda-avanzada?cliente=Ana&totalMinimo=1000' $consulta

Paso 'La misma búsqueda en MySQL devuelve solo ese pedido'
MySql "SELECT id, cliente, total FROM pedido WHERE cliente LIKE '%Ana%' AND total >= 1000 ORDER BY id DESC"

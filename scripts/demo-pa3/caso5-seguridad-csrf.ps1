. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 5 - Seguridad por roles y CSRF'

Paso 'Anónimo: reportes redirige al login'
Get '/reportes/pedidos'
Get '/api/v1/pedidos/clientes-frecuentes?minPedidos=2'

Paso 'CONSULTA: puede ver reportes'
$consulta = Login 'consulta'
Get '/reportes/pedidos' $consulta
Get '/api/v1/pedidos/clientes-frecuentes?minPedidos=2' $consulta

Paso 'CONSULTA con CSRF válido: NO puede registrar (403 por rol)'
RegistrarPedido $consulta (TokenCsrf $consulta) 'Prueba Consulta' 'TEC-001' 1

Paso 'OPERADOR sin CSRF: solicitud rechazada (403 por CSRF)'
$operador = Login 'operador'
RegistrarPedido $operador '' 'Prueba Sin Token' 'TEC-001' 1

Paso 'Inventario restaurado. OPERADOR con CSRF: registra correctamente'
Get "$Inventario/productos"
RegistrarPedido $operador (TokenCsrf $operador) 'Carlos Rojas' 'TEC-001' 1

Paso 'Los dos intentos rechazados no quedaron en MySQL'
MySql 'SELECT id, cliente, total FROM pedido ORDER BY id DESC'

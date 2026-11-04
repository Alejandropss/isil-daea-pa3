<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reportes de pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">Spring MVC · Spring Security · Spring JDBC</p>
            <h1>Reportes de pedidos</h1>
            <p class="subtitulo">Consultas de solo lectura sobre la base local de pedidos.</p>
        </div>
        <div class="acciones">
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}">Ir a pedidos</a>
            <c:url var="logoutUrl" value="/logout"/>
            <form method="post" action="${logoutUrl}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Cerrar sesión</button>
            </form>
        </div>
    </header>

    <c:if test="${not empty error}">
        <div class="alerta error"><c:out value="${error}"/></div>
    </c:if>

    <section class="tarjeta">
        <h2>Resumen general</h2>
        <div class="resumen-grid">
            <div><span>Pedidos</span><strong><c:out value="${resumen.totalPedidos()}"/></strong></div>
            <div><span>Unidades</span><strong><c:out value="${resumen.totalUnidades()}"/></strong></div>
            <div><span>Importe acumulado</span>
                <strong>S/ <fmt:formatNumber value="${resumen.importeAcumulado()}" minFractionDigits="2" maxFractionDigits="2"/></strong></div>
            <div><span>Ticket promedio</span>
                <strong>S/ <fmt:formatNumber value="${resumen.ticketPromedio()}" minFractionDigits="2" maxFractionDigits="2"/></strong></div>
        </div>
    </section>

    <c:url var="reportesUrl" value="/reportes/pedidos"/>

    <section class="tarjeta">
        <h2>Búsqueda por cliente y total mínimo</h2>
        <form method="get" action="${reportesUrl}" class="form-reporte">
            <label>Cliente
                <input name="cliente" maxlength="120" placeholder="Ej. Ana" value="${fn:escapeXml(clienteIngresado)}">
            </label>
            <label>Total mínimo (S/)
                <input name="totalMinimo" placeholder="Ej. 1000" value="${fn:escapeXml(totalMinimoIngresado)}">
            </label>
            <c:if test="${not empty minPedidosIngresado}">
                <input type="hidden" name="minPedidos" value="${fn:escapeXml(minPedidosIngresado)}">
            </c:if>
            <button type="submit">Buscar</button>
        </form>

        <c:if test="${pedidos != null}">
            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr><th>ID</th><th>Cliente</th><th>Producto</th><th>Cantidad</th><th>Total</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pedido" items="${pedidos}">
                        <tr>
                            <td><c:out value="${pedido.id}"/></td>
                            <td><c:out value="${pedido.cliente}"/></td>
                            <td><c:out value="${pedido.productoNombre}"/></td>
                            <td><c:out value="${pedido.cantidad}"/></td>
                            <td>S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pedidos}">
                        <tr><td colspan="5" class="sin-datos">No hay pedidos con esos filtros.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </c:if>
    </section>

    <section class="tarjeta">
        <h2>Clientes frecuentes</h2>
        <form method="get" action="${reportesUrl}" class="form-reporte">
            <label>Mínimo de pedidos
                <input name="minPedidos" placeholder="Ej. 2" value="${fn:escapeXml(minPedidosIngresado)}">
            </label>
            <c:if test="${not empty clienteIngresado or not empty totalMinimoIngresado}">
                <input type="hidden" name="cliente" value="${fn:escapeXml(clienteIngresado)}">
                <input type="hidden" name="totalMinimo" value="${fn:escapeXml(totalMinimoIngresado)}">
            </c:if>
            <button type="submit">Consultar</button>
        </form>

        <c:if test="${clientesFrecuentes != null}">
            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr><th>Cliente</th><th>Pedidos</th><th>Unidades</th><th>Importe acumulado</th><th>Ticket promedio</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="frecuente" items="${clientesFrecuentes}">
                        <tr>
                            <td><c:out value="${frecuente.cliente()}"/></td>
                            <td><c:out value="${frecuente.totalPedidos()}"/></td>
                            <td><c:out value="${frecuente.totalUnidades()}"/></td>
                            <td>S/ <fmt:formatNumber value="${frecuente.importeAcumulado()}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td>S/ <fmt:formatNumber value="${frecuente.ticketPromedio()}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty clientesFrecuentes}">
                        <tr><td colspan="5" class="sin-datos">Ningún cliente cumple ese mínimo.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </c:if>
    </section>
</div>
</body>
</html>

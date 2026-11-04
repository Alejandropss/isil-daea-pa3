package com.isil.edu.pe.spedidos.repository.projection;

import java.math.BigDecimal;

/**
 * Resultado agregado por cliente (GROUP BY cliente).
 */
public record ClienteFrecuenteProjection(
    String cliente,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}

package com.isil.edu.pe.spedidos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** PA3: las dos consultas nuevas de Spring JDBC, probadas contra H2. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteJdbcPa3Test {

  @Autowired
  private PedidoReporteJdbcRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("DELETE FROM pedido");
    insertarPedido("Ana Torres", "LAP-001", "Laptop", "2500.00", 2, "5000.00");
    insertarPedido("Ana Torres", "TEC-001", "Teclado", "120.00", 1, "120.00");
    insertarPedido("Carlos Rojas", "MON-001", "Monitor", "850.00", 3, "2550.00");
  }

  @Test
  @DisplayName("Return orders from PedidoReporteJdbcRepository when customer and minimum total match")
  void returnOrdersFromPedidoReporteJdbcRepositoryWhenCustomerAndMinimumTotalMatch() {
    // Arrange
    insertarPedido("Ana Torres", "MON-001", "Monitor", "850.00", 2, "1700.00");

    // Act
    List<Pedido> pedidos = repository.buscarPedidosPorClienteYTotalMinimo("ana", new BigDecimal("1000"));

    // Assert: el de S/ 120 y el de Carlos quedan fuera.
    assertEquals(2, pedidos.size());
    assertTrue(pedidos.stream().allMatch(pedido -> "Ana Torres".equals(pedido.getCliente())));
    assertEquals(0, new BigDecimal("1700.00").compareTo(pedidos.get(0).getTotal()));
    assertTrue(pedidos.get(0).getId() > pedidos.get(1).getId());
  }

  @Test
  @DisplayName("Return no orders from PedidoReporteJdbcRepository when customer contains SQL text")
  void returnNoOrdersFromPedidoReporteJdbcRepositoryWhenCustomerContainsSqlText() {
    // Arrange
    String intento = "' OR '1'='1";

    // Act
    List<Pedido> pedidos = repository.buscarPedidosPorClienteYTotalMinimo(intento, BigDecimal.ZERO);

    // Assert: el texto viaja como parámetro, no como parte del SQL.
    assertTrue(pedidos.isEmpty());
  }

  @Test
  @DisplayName("Return aggregated values from PedidoReporteJdbcRepository when customer reaches minimum orders")
  void returnAggregatedValuesFromPedidoReporteJdbcRepositoryWhenCustomerReachesMinimumOrders() {
    // Arrange: Ana tiene 2 pedidos y Carlos solo 1.
    int minPedidos = 2;

    // Act
    List<ClienteFrecuenteProjection> clientes = repository.listarClientesFrecuentes(minPedidos);

    // Assert
    assertEquals(1, clientes.size());
    ClienteFrecuenteProjection ana = clientes.get(0);
    assertEquals("Ana Torres", ana.cliente());
    assertEquals(2L, ana.totalPedidos());
    assertEquals(3L, ana.totalUnidades());
    assertEquals(0, new BigDecimal("5120.00").compareTo(ana.importeAcumulado()));
    assertEquals(0, new BigDecimal("2560.00").compareTo(ana.ticketPromedio()));
  }

  private void insertarPedido(String cliente, String codigo, String nombre, String precio, int cantidad,
      String total) {
    jdbcTemplate.update(
        """
        INSERT INTO pedido (cliente, producto_codigo, producto_nombre, precio_unitario, cantidad, total, fecha)
        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        cliente, codigo, nombre, new BigDecimal(precio), cantidad, new BigDecimal(total));
  }
}

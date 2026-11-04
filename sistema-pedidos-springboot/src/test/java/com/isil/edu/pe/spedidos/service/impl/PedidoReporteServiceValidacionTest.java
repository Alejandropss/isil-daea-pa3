package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** PA3: el Service rechaza valores inválidos antes de llegar al repositorio. */
class PedidoReporteServiceValidacionTest {

  private PedidoReporteJdbcRepository repository;
  private PedidoReporteServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(PedidoReporteJdbcRepository.class);
    service = new PedidoReporteServiceImpl(repository);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  @DisplayName("Reject advanced search in PedidoReporteService when customer is empty")
  void rejectAdvancedSearchInPedidoReporteServiceWhenCustomerIsEmpty(String cliente) {
    // Act
    PedidoException error = assertThrows(PedidoException.class,
        () -> service.buscarPedidosPorClienteYTotalMinimo(cliente, BigDecimal.TEN));

    // Assert
    assertEquals("El cliente es obligatorio.", error.getMessage());
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Reject advanced search in PedidoReporteService when minimum total is negative")
  void rejectAdvancedSearchInPedidoReporteServiceWhenMinimumTotalIsNegative() {
    // Arrange
    BigDecimal negativo = new BigDecimal("-1");

    // Act
    PedidoException error = assertThrows(PedidoException.class,
        () -> service.buscarPedidosPorClienteYTotalMinimo("Ana", negativo));

    // Assert
    assertEquals("El total mínimo debe ser mayor o igual a 0.", error.getMessage());
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Reject frequent customers in PedidoReporteService when minimum orders is zero")
  void rejectFrequentCustomersInPedidoReporteServiceWhenMinimumOrdersIsZero() {
    // Act
    PedidoException error = assertThrows(PedidoException.class, () -> service.listarClientesFrecuentes(0));

    // Assert
    assertEquals("minPedidos debe ser mayor o igual a 1.", error.getMessage());
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Return frequent customers from PedidoReporteService when minimum orders is valid")
  void returnFrequentCustomersFromPedidoReporteServiceWhenMinimumOrdersIsValid() {
    // Arrange
    when(repository.listarClientesFrecuentes(2)).thenReturn(List.of(
        new ClienteFrecuenteProjection("Ana Torres", 2, 3, new BigDecimal("5120.00"), new BigDecimal("2560.00"))));

    // Act
    List<ClienteFrecuente> clientes = service.listarClientesFrecuentes(2);

    // Assert
    assertEquals(1, clientes.size());
    assertEquals("Ana Torres", clientes.get(0).cliente());
    assertEquals(0, new BigDecimal("2560.00").compareTo(clientes.get(0).ticketPromedio()));
    verify(repository).listarClientesFrecuentes(2);
  }
}

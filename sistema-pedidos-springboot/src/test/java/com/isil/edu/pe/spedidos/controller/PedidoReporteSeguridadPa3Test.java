package com.isil.edu.pe.spedidos.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** PA3: la pantalla de reportes y la API con los filtros de seguridad reales. */
@SpringBootTest
@ActiveProfiles("test")
class PedidoReporteSeguridadPa3Test {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @MockitoSpyBean
  private PedidoReporteService reporteService;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("Show reports view from PedidoReporteWebController when CONSULTA requests the page")
  void showReportsViewFromPedidoReporteWebControllerWhenConsultaRequestsThePage() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos")
        .param("minPedidos", "2")
        .with(user("consulta").roles("CONSULTA")));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(forwardedUrl("/WEB-INF/views/reportes-pedidos.jsp"))
        .andExpect(model().attributeExists("resumen", "clientesFrecuentes"));
  }

  @Test
  @DisplayName("Redirect PedidoReporteWebController request to login when user is anonymous")
  void redirectPedidoReporteWebControllerRequestToLoginWhenUserIsAnonymous() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos"));

    // Assert: el servicio no se ejecuta, seguridad corta antes.
    response.andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @DisplayName("Return JSON list from PedidoReporteController when CONSULTA requests frequent customers")
  void returnJsonListFromPedidoReporteControllerWhenConsultaRequestsFrequentCustomers() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .param("minPedidos", "1")
        .with(user("consulta").roles("CONSULTA")));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("Return 400 from PedidoReporteController when parameters are negative, not numeric or missing")
  void return400FromPedidoReporteControllerWhenParametersAreNegativeNotNumericOrMissing() throws Exception {
    // Arrange / Act
    ResultActions negativo = mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
        .param("cliente", "Ana").param("totalMinimo", "-1")
        .with(user("consulta").roles("CONSULTA")));
    ResultActions noNumerico = mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
        .param("cliente", "Ana").param("totalMinimo", "abc")
        .with(user("consulta").roles("CONSULTA")));
    ResultActions faltante = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .with(user("consulta").roles("CONSULTA")));

    // Assert
    negativo.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El total mínimo debe ser mayor o igual a 0."));
    noNumerico.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    faltante.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Falta el parámetro minPedidos."));
  }

  @Test
  @DisplayName("Return 400 from PedidoReporteWebController when minimum orders is invalid")
  void return400FromPedidoReporteWebControllerWhenMinimumOrdersIsInvalid() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos")
        .param("minPedidos", "0")
        .with(user("consulta").roles("CONSULTA")));

    // Assert: la pantalla muestra el error y conserva el resumen.
    response.andExpect(status().isBadRequest())
        .andExpect(forwardedUrl("/WEB-INF/views/reportes-pedidos.jsp"))
        .andExpect(model().attribute("error", "minPedidos debe ser mayor o igual a 1."))
        .andExpect(model().attributeExists("resumen"));
  }

  @Test
  @DisplayName("Return 503 from PedidoReporteController when database fails")
  void return503FromPedidoReporteControllerWhenDatabaseFails() throws Exception {
    // Arrange
    doThrow(new DataAccessResourceFailureException("MySQL detenido"))
        .when(reporteService).listarClientesFrecuentes(anyInt());

    // Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .param("minPedidos", "2")
        .with(user("consulta").roles("CONSULTA")));

    // Assert
    response.andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.message").value("La base de datos no está disponible."));
  }
}

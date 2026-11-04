package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Pantalla JSP de reportes. Usa el mismo Service que la API JSON,
 * así las validaciones y el SQL no se repiten aquí.
 */
@Controller
@RequestMapping("/reportes/pedidos")
public class PedidoReporteWebController {

  private static final Logger log = LoggerFactory.getLogger(PedidoReporteWebController.class);

  private final PedidoReporteService reporteService;

  public PedidoReporteWebController(PedidoReporteService reporteService) {
    this.reporteService = reporteService;
  }

  @GetMapping
  public String verReportes(
      @RequestParam(required = false) String cliente,
      @RequestParam(required = false) String totalMinimo,
      @RequestParam(required = false) String minPedidos,
      Model model,
      HttpServletResponse response) {

    model.addAttribute("clienteIngresado", cliente);
    model.addAttribute("totalMinimoIngresado", totalMinimo);
    model.addAttribute("minPedidosIngresado", minPedidos);

    try {
      model.addAttribute("resumen", reporteService.obtenerResumen());
      cargarConsultas(cliente, totalMinimo, minPedidos, model);
    } catch (PedidoException e) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      model.addAttribute("error", e.getMessage());
    } catch (DataAccessException e) {
      log.error("Error al consultar reportes.", e);
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
      model.addAttribute("error", "La base de datos no está disponible.");
      return "error";
    }
    return "reportes-pedidos";
  }

  // Cada consulta se ejecuta solo si se envió su formulario.
  private void cargarConsultas(String cliente, String totalMinimo, String minPedidos, Model model) {
    if (cliente != null || totalMinimo != null) {
      model.addAttribute("pedidos",
          reporteService.buscarPedidosPorClienteYTotalMinimo(cliente, aDecimal(totalMinimo)));
    }
    if (minPedidos != null) {
      model.addAttribute("clientesFrecuentes",
          reporteService.listarClientesFrecuentes(aEntero(minPedidos)));
    }
  }

  private BigDecimal aDecimal(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(valor.trim());
    } catch (NumberFormatException e) {
      throw new PedidoException("El total mínimo debe ser un número.", e);
    }
  }

  private int aEntero(String valor) {
    try {
      return Integer.parseInt(valor.trim());
    } catch (NumberFormatException e) {
      throw new PedidoException("minPedidos debe ser un número entero.", e);
    }
  }
}

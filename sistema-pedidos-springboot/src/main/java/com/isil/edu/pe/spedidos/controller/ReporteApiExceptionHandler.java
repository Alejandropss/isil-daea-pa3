package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.ApiErrorResponse;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte los errores de la API de reportes en JSON con su código HTTP.
 */
@RestControllerAdvice(assignableTypes = PedidoReporteController.class)
public class ReporteApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ReporteApiExceptionHandler.class);

  @ExceptionHandler(PedidoException.class)
  public ResponseEntity<ApiErrorResponse> parametroInvalido(PedidoException e, HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, e.getMessage(), req);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorResponse> parametroFaltante(MissingServletRequestParameterException e,
      HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, "Falta el parámetro " + e.getParameterName() + ".", req);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException e,
      HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, "El parámetro " + e.getName() + " no tiene un valor válido.", req);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiErrorResponse> baseDeDatos(DataAccessException e, HttpServletRequest req) {
    log.error("Error al consultar reportes.", e);
    return responder(HttpStatus.SERVICE_UNAVAILABLE, "La base de datos no está disponible.", req);
  }

  private static ResponseEntity<ApiErrorResponse> responder(HttpStatus status, String mensaje,
      HttpServletRequest req) {
    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI()));
  }
}

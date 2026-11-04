package com.isil.edu.pe.spedidos.controller.dto;

/**
 * JSON de error de la API de reportes.
 */
public record ApiErrorResponse(int status, String error, String message, String path) {
}

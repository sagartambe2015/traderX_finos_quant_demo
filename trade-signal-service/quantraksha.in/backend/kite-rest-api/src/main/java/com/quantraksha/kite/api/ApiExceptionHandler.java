package com.quantraksha.kite.api;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Errors use the same shape as Kite's: {"error_type": ..., "message": ...}. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(KiteApiException.class)
    public ResponseEntity<Map<String, String>> kite(KiteApiException e) {
        return ResponseEntity.status(e.status())
                .body(Map.of("error_type", e.errorType(), "message", String.valueOf(e.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error_type", "InputException", "message", String.valueOf(e.getMessage())));
    }
}

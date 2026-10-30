package com.quantraksha.kite.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Errors use Kite's shape: {"error_type": ..., "message": ...}. */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void kiteExceptionKeepsItsStatusAndType() {
        ResponseEntity<Map<String, String>> response =
                handler.kite(new KiteApiException(HttpStatus.UNAUTHORIZED, "TokenException", "Token expired"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .containsEntry("error_type", "TokenException")
                .containsEntry("message", "Token expired");
    }

    @Test
    void illegalArgumentIsABadRequestInputException() {
        ResponseEntity<Map<String, String>> response =
                handler.badRequest(new IllegalArgumentException("days must be 1 to 20"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error_type", "InputException")
                .containsEntry("message", "days must be 1 to 20");
    }

    @Test
    void nullMessageIsRenderedAsTextNotDropped() {
        ResponseEntity<Map<String, String>> response =
                handler.kite(new KiteApiException(HttpStatus.BAD_GATEWAY, "NetworkException", null));

        assertThat(response.getBody()).containsEntry("message", "null");
    }
}

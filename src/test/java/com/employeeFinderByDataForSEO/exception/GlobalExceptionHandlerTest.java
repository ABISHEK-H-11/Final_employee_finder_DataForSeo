package com.employeeFinderByDataForSEO.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturn429WhenDailyQuotaIsExceeded() {

        DailyQuotaExceededException exception =
                new DailyQuotaExceededException(
                        "Daily employee profile quota exceeded"
                );

        GlobalExceptionHandler handler =
                new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleDailyQuotaExceeded(exception);

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                response.getStatusCode()
        );

        assertEquals(
                429,
                response.getBody().get("status")
        );

        assertEquals(
                "Too Many Requests",
                response.getBody().get("error")
        );

        assertEquals(
                "Daily employee profile quota exceeded",
                response.getBody().get("message")
        );
    }
}
package com.employeeFinderByDataForSEO.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DailyQuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleDailyQuotaExceeded(
            DailyQuotaExceededException ex) {

        Map<String, Object> response = Map.of(
                "status", 429,
                "error", "Too Many Requests",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response);
    }

}

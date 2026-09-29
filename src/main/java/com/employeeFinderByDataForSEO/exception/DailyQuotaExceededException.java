package com.employeeFinderByDataForSEO.exception;

public class DailyQuotaExceededException extends RuntimeException{
    public DailyQuotaExceededException(String message) {
        super(message);
    }
}

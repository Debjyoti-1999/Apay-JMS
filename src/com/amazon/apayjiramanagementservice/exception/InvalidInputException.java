package com.amazon.apayjiramanagementservice.exception;

/**
 * Exception thrown when invalid input is provided to a service method.
 */
public class InvalidInputException extends RuntimeException {
    /**
     * @param message the detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }
    /**
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param cause the cause of the exception
     */
    public InvalidInputException(Throwable cause) {
        super(cause);
    }
}

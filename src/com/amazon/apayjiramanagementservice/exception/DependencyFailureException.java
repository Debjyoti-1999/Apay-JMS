package com.amazon.apayjiramanagementservice.exception;


public class DependencyFailureException extends RuntimeException {
    /**
     * @param message the detail message
     */
    public DependencyFailureException(String message) {
        super(message);
    }
    /**
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DependencyFailureException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param cause the cause of the exception
     */
    public DependencyFailureException(Throwable cause) {
        super(cause);
    }
}

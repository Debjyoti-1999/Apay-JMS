package com.amazon.apayjiramanagementservice.exception;


public class InternalServiceException extends RuntimeException {
    /**
     * @param message the detail message
     */
    public InternalServiceException(String message) {
        super(message);
    }
    /**
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param cause the cause of the exception
     */
    public InternalServiceException(Throwable cause) {
        super(cause);
    }
}

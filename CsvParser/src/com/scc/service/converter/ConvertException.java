package com.scc.service.converter;

public class ConvertException extends RuntimeException {
    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}

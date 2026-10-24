package com.example.catalog.common.error;

/**
 * Thrown when a requested entity does not exist. Mapped to HTTP 404 by the
 * {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s with id %s not found".formatted(resource, id));
    }
}

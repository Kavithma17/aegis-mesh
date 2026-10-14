package com.aegismesh.request.exception;

import java.util.UUID;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(UUID id) {
        super("Medicine request not found: " + id);
    }
}

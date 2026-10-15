package com.mvimbi.aggregator.web;

/** Thrown when an id doesn't exist, mapped to HTTP 404. @author Athenkosi Mvimbi */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

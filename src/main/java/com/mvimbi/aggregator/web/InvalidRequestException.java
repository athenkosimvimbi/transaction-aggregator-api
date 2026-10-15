package com.mvimbi.aggregator.web;

/** Thrown for bad input that Bean Validation can't express (e.g. from after to), mapped to HTTP 400. @author Athenkosi Mvimbi */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}

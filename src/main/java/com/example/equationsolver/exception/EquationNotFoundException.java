package com.example.equationsolver.exception;

public class EquationNotFoundException extends RuntimeException {

    public EquationNotFoundException(String id) {
        super("No equation found with id '" + id + "'");
    }
}

package com.example.equationsolver.exception;

import java.util.List;

/** Thrown when an equation is evaluated without a value for every variable it uses. */
public class MissingVariablesException extends InvalidRequestException {

    private final List<String> missing;

    public MissingVariablesException(List<String> missing) {
        super("Values are missing for variable(s): " + String.join(", ", missing));
        this.missing = List.copyOf(missing);
    }

    public List<String> getMissing() {
        return missing;
    }
}

package com.example.equationsolver.tree;

import com.example.equationsolver.exception.MissingVariablesException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record VariableNode(String name) implements ExprNode {

    @Override
    public double evaluate(Map<String, Double> variables) {
        Double value = variables.get(name);
        if (value == null) {
            throw new MissingVariablesException(List.of(name));
        }
        return value;
    }

    @Override
    public String toInfix() {
        return name;
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void collectVariables(Set<String> into) {
        into.add(name);
    }
}

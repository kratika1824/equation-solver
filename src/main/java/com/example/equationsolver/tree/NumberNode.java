package com.example.equationsolver.tree;

import com.example.equationsolver.util.NumberFormatter;

import java.util.Map;
import java.util.Set;

public record NumberNode(double value) implements ExprNode {

    @Override
    public double evaluate(Map<String, Double> variables) {
        return value;
    }

    @Override
    public String toInfix() {
        return NumberFormatter.format(value);
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void collectVariables(Set<String> into) {
        // a number has no variables
    }
}

package com.example.equationsolver.tree;

import com.example.equationsolver.parser.Operator;

import java.util.Map;
import java.util.Set;

/** A node with a single child, used for unary minus (-x). */
public record UnaryNode(Operator operator, ExprNode operand) implements ExprNode {

    @Override
    public double evaluate(Map<String, Double> variables) {
        return operator.apply(operand.evaluate(variables));
    }

    @Override
    public String toInfix() {
        String inner = operand.toInfix();
        if (operand.precedence() <= operator.precedence()) {
            inner = "(" + inner + ")";
        }
        return "-" + inner;
    }

    @Override
    public int precedence() {
        return operator.precedence();
    }

    @Override
    public void collectVariables(Set<String> into) {
        operand.collectVariables(into);
    }
}

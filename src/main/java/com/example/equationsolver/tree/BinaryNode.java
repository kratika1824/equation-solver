package com.example.equationsolver.tree;

import com.example.equationsolver.parser.Operator;

import java.util.Map;
import java.util.Set;

/** An operator with a left and a right child, e.g. the '+' in a + b. */
public record BinaryNode(Operator operator, ExprNode left, ExprNode right) implements ExprNode {

    @Override
    public double evaluate(Map<String, Double> variables) {
        // children first, then the operator: that is what makes it a postfix evaluation
        return operator.apply(left.evaluate(variables), right.evaluate(variables));
    }

    @Override
    public String toInfix() {
        if (isImplicitProduct()) {
            return left.toInfix() + right.toInfix();
        }
        String leftText = bracketIfNeeded(left, true);
        String rightText = bracketIfNeeded(right, false);
        String gap = operator == Operator.POWER ? "" : " ";
        return leftText + gap + operator.symbol() + gap + rightText;
    }

    @Override
    public int precedence() {
        return operator.precedence();
    }

    @Override
    public void collectVariables(Set<String> into) {
        left.collectVariables(into);
        right.collectVariables(into);
    }

    /** Shows 3 * x as 3x and 2 * x^2 as 2x^2, the way people write algebra. */
    private boolean isImplicitProduct() {
        if (operator != Operator.MULTIPLY || !(left instanceof NumberNode)) {
            return false;
        }
        if (right instanceof VariableNode) {
            return true;
        }
        return right instanceof BinaryNode power
                && power.operator == Operator.POWER
                && power.left instanceof VariableNode;
    }

    private String bracketIfNeeded(ExprNode child, boolean isLeftChild) {
        String text = child.toInfix();
        boolean needsBrackets;
        if (child.precedence() < operator.precedence()) {
            needsBrackets = true;
        } else if (child.precedence() == operator.precedence()) {
            // a - (b - c) needs brackets on the right, (a ^ b) ^ c needs them on the left
            needsBrackets = isLeftChild ? operator.isRightAssociative() : !operator.isRightAssociative();
        } else {
            needsBrackets = false;
        }
        return needsBrackets ? "(" + text + ")" : text;
    }
}

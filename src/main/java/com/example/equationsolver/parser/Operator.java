package com.example.equationsolver.parser;

import com.example.equationsolver.exception.EvaluationException;

import java.util.Optional;

/**
 * Every operator the calculator understands, with its precedence and associativity.
 * Higher precedence binds tighter. NEGATE is the unary minus (as in -x).
 */
public enum Operator {
    ADD('+', 1, false, 2),
    SUBTRACT('-', 1, false, 2),
    MULTIPLY('*', 2, false, 2),
    DIVIDE('/', 2, false, 2),
    NEGATE('-', 3, true, 1),
    POWER('^', 4, true, 2);

    private final char symbol;
    private final int precedence;
    private final boolean rightAssociative;
    private final int arity;

    Operator(char symbol, int precedence, boolean rightAssociative, int arity) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.rightAssociative = rightAssociative;
        this.arity = arity;
    }

    public char symbol() {
        return symbol;
    }

    public int precedence() {
        return precedence;
    }

    public boolean isRightAssociative() {
        return rightAssociative;
    }

    /** Number of operands: 1 for unary minus, 2 for everything else. */
    public int arity() {
        return arity;
    }

    /** Text used when printing postfix. Unary minus is "neg" so it is not confused with subtraction. */
    public String display() {
        return this == NEGATE ? "neg" : String.valueOf(symbol);
    }

    /** Looks up a binary operator by its character. A '-' always comes back as SUBTRACT. */
    public static Optional<Operator> fromSymbol(char c) {
        switch (c) {
            case '+':
                return Optional.of(ADD);
            case '-':
                return Optional.of(SUBTRACT);
            case '*':
                return Optional.of(MULTIPLY);
            case '/':
                return Optional.of(DIVIDE);
            case '^':
                return Optional.of(POWER);
            default:
                return Optional.empty();
        }
    }

    public double apply(double left, double right) {
        double result;
        switch (this) {
            case ADD:
                result = left + right;
                break;
            case SUBTRACT:
                result = left - right;
                break;
            case MULTIPLY:
                result = left * right;
                break;
            case DIVIDE:
                if (right == 0) {
                    throw new EvaluationException("Division by zero");
                }
                result = left / right;
                break;
            case POWER:
                result = Math.pow(left, right);
                break;
            default:
                throw new IllegalStateException(this + " is not a binary operator");
        }
        return checkResult(result);
    }

    public double apply(double value) {
        if (this != NEGATE) {
            throw new IllegalStateException(this + " is not a unary operator");
        }
        return -value;
    }

    private static double checkResult(double result) {
        if (Double.isNaN(result)) {
            throw new EvaluationException("Result is not a real number");
        }
        if (Double.isInfinite(result)) {
            throw new EvaluationException("Result is undefined or too large to represent");
        }
        return result;
    }
}

package com.example.equationsolver.parser;

import com.example.equationsolver.exception.InvalidEquationException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Turns an infix equation such as "3x + 2y - z" into postfix order using the shunting-yard
 * algorithm. Along the way it also checks the structure of the equation, so every error
 * message can point at a position in the original text.
 */
public class InfixToPostfixConverter {

    static final int MAX_LENGTH = 500;

    private final Tokenizer tokenizer;

    public InfixToPostfixConverter(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public List<Token> convert(String equation) {
        if (equation == null || equation.isBlank()) {
            throw new InvalidEquationException("Equation must not be empty");
        }
        if (equation.length() > MAX_LENGTH) {
            throw new InvalidEquationException("Equation is too long (maximum " + MAX_LENGTH + " characters)");
        }

        List<Token> tokens = tokenizer.tokenize(equation);
        List<Token> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();

        // true whenever the next token has to be a number, a variable, '(' or a unary sign
        boolean expectOperand = true;

        for (Token token : tokens) {
            switch (token.type()) {
                case NUMBER:
                    if (!expectOperand) {
                        throw new InvalidEquationException("Unexpected number '" + token.text() + "' at position "
                                + token.position() + " (is an operator missing?)");
                    }
                    output.add(token);
                    expectOperand = false;
                    break;

                case VARIABLE:
                    if (!expectOperand) {
                        // "3x" or "xy" -> there is a hidden multiplication between the two
                        pushOperator(Token.ofOperator(Operator.MULTIPLY, token.position()), stack, output);
                    }
                    output.add(token);
                    expectOperand = false;
                    break;

                case OPERATOR:
                    expectOperand = handleOperator(token, expectOperand, stack, output);
                    break;

                case LEFT_PAREN:
                    if (!expectOperand) {
                        // "2(x+1)" -> hidden multiplication again
                        pushOperator(Token.ofOperator(Operator.MULTIPLY, token.position()), stack, output);
                    }
                    stack.push(token);
                    expectOperand = true;
                    break;

                case RIGHT_PAREN:
                    if (expectOperand) {
                        throw new InvalidEquationException("Missing value before ')' at position " + token.position());
                    }
                    popUntilLeftParen(token, stack, output);
                    expectOperand = false;
                    break;

                default:
                    throw new IllegalStateException("Unknown token type " + token.type());
            }
        }

        if (expectOperand) {
            throw new InvalidEquationException("Equation is incomplete: a value is missing at the end");
        }

        while (!stack.isEmpty()) {
            Token top = stack.pop();
            if (top.type() == TokenType.LEFT_PAREN) {
                throw new InvalidEquationException("Unmatched '(' at position " + top.position());
            }
            output.add(top);
        }
        return output;
    }

    /** Joins postfix tokens with spaces, e.g. "3 x * 2 y * + z -". */
    public static String format(List<Token> postfix) {
        StringBuilder sb = new StringBuilder();
        for (Token token : postfix) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(token.text());
        }
        return sb.toString();
    }

    private boolean handleOperator(Token token, boolean expectOperand, Deque<Token> stack, List<Token> output) {
        Operator operator = token.operator();

        if (expectOperand) {
            // nothing on the left, so '-' and '+' are signs, anything else is an error
            if (operator == Operator.SUBTRACT) {
                stack.push(Token.ofOperator(Operator.NEGATE, token.position()));
            } else if (operator != Operator.ADD) {
                throw new InvalidEquationException("Operator '" + operator.symbol() + "' at position "
                        + token.position() + " has no value before it");
            }
            return true;
        }

        pushOperator(token, stack, output);
        return true;
    }

    /** Moves operators that must run first from the stack to the output, then pushes this one. */
    private void pushOperator(Token incoming, Deque<Token> stack, List<Token> output) {
        Operator current = incoming.operator();
        while (!stack.isEmpty() && stack.peek().type() == TokenType.OPERATOR) {
            Operator top = stack.peek().operator();
            boolean topRunsFirst = top.precedence() > current.precedence()
                    || (top.precedence() == current.precedence() && !current.isRightAssociative());
            if (!topRunsFirst) {
                break;
            }
            output.add(stack.pop());
        }
        stack.push(incoming);
    }

    private void popUntilLeftParen(Token closing, Deque<Token> stack, List<Token> output) {
        while (!stack.isEmpty()) {
            Token top = stack.pop();
            if (top.type() == TokenType.LEFT_PAREN) {
                return;
            }
            output.add(top);
        }
        throw new InvalidEquationException("Unmatched ')' at position " + closing.position());
    }
}

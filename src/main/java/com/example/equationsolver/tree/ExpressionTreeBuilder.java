package com.example.equationsolver.tree;

import com.example.equationsolver.exception.InvalidEquationException;
import com.example.equationsolver.parser.Operator;
import com.example.equationsolver.parser.Token;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Builds the expression tree from postfix tokens using a stack:
 * operands are pushed, and an operator pops its children and pushes the new parent.
 */
public class ExpressionTreeBuilder {

    public ExprNode build(List<Token> postfix) {
        Deque<ExprNode> stack = new ArrayDeque<>();

        for (Token token : postfix) {
            switch (token.type()) {
                case NUMBER:
                    stack.push(new NumberNode(parseNumber(token)));
                    break;
                case VARIABLE:
                    stack.push(new VariableNode(token.text()));
                    break;
                case OPERATOR:
                    Operator operator = token.operator();
                    if (operator.arity() == 1) {
                        stack.push(new UnaryNode(operator, popOperand(stack, token)));
                    } else {
                        ExprNode right = popOperand(stack, token);
                        ExprNode left = popOperand(stack, token);
                        stack.push(new BinaryNode(operator, left, right));
                    }
                    break;
                default:
                    throw new InvalidEquationException("Unexpected '" + token.text() + "' in postfix expression");
            }
        }

        if (stack.size() != 1) {
            throw new InvalidEquationException("Equation is not a valid expression");
        }
        return stack.pop();
    }

    private double parseNumber(Token token) {
        double value = Double.parseDouble(token.text());
        if (Double.isInfinite(value)) {
            throw new InvalidEquationException("Number '" + token.text() + "' at position "
                    + token.position() + " is too large");
        }
        return value;
    }

    private ExprNode popOperand(Deque<ExprNode> stack, Token operatorToken) {
        if (stack.isEmpty()) {
            throw new InvalidEquationException("Operator '" + operatorToken.text() + "' at position "
                    + operatorToken.position() + " is missing a value");
        }
        return stack.pop();
    }
}

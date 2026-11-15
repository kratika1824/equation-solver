package com.example.equationsolver.tree;

import java.util.Map;
import java.util.Set;

/**
 * A node of the expression (postfix) tree. Operators are parent nodes,
 * numbers and variables are the leaves.
 */
public sealed interface ExprNode permits NumberNode, VariableNode, UnaryNode, BinaryNode {

    double evaluate(Map<String, Double> variables);

    /** Rebuilds readable infix text from the tree, adding brackets only where they are needed. */
    String toInfix();

    /** How tightly this node binds. Leaves return the maximum so they never need brackets. */
    int precedence();

    void collectVariables(Set<String> into);
}

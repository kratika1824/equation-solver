package com.example.equationsolver.model;

import com.example.equationsolver.tree.ExprNode;

/** An equation kept in memory: its id, the expression tree, and the postfix form it was built from. */
public record StoredEquation(long id, ExprNode root, String postfix) {

    /** Infix text rebuilt from the tree (not the text the user typed). */
    public String infix() {
        return root.toInfix();
    }
}

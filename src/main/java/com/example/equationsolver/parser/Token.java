package com.example.equationsolver.parser;

/**
 * One piece of the equation text. Position is 1-based so error messages match what a person counts.
 * The operator field is only set for OPERATOR tokens.
 */
public record Token(TokenType type, String text, int position, Operator operator) {

    public static Token number(String text, int position) {
        return new Token(TokenType.NUMBER, text, position, null);
    }

    public static Token variable(String name, int position) {
        return new Token(TokenType.VARIABLE, name, position, null);
    }

    public static Token ofOperator(Operator operator, int position) {
        return new Token(TokenType.OPERATOR, operator.display(), position, operator);
    }

    public static Token leftParen(int position) {
        return new Token(TokenType.LEFT_PAREN, "(", position, null);
    }

    public static Token rightParen(int position) {
        return new Token(TokenType.RIGHT_PAREN, ")", position, null);
    }
}

package com.example.equationsolver.parser;

import com.example.equationsolver.exception.InvalidEquationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Splits the raw equation text into tokens. It only checks characters, not the structure. */
public class Tokenizer {

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);
            int position = i + 1;

            if (Character.isWhitespace(c)) {
                i++;
            } else if (isDigit(c) || c == '.') {
                int start = i;
                int dots = 0;
                while (i < input.length() && (isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    if (input.charAt(i) == '.') {
                        dots++;
                    }
                    i++;
                }
                String text = input.substring(start, i);
                if (dots > 1 || text.equals(".")) {
                    throw new InvalidEquationException("Invalid number '" + text + "' at position " + position);
                }
                tokens.add(Token.number(text, position));
            } else if (isLetter(c)) {
                // variables are always a single letter, so "xy" means x * y
                tokens.add(Token.variable(String.valueOf(c), position));
                i++;
            } else if (c == '(') {
                tokens.add(Token.leftParen(position));
                i++;
            } else if (c == ')') {
                tokens.add(Token.rightParen(position));
                i++;
            } else {
                Optional<Operator> operator = Operator.fromSymbol(c);
                if (operator.isEmpty()) {
                    throw new InvalidEquationException("Unexpected character '" + c + "' at position " + position);
                }
                tokens.add(Token.ofOperator(operator.get(), position));
                i++;
            }
        }
        return tokens;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}

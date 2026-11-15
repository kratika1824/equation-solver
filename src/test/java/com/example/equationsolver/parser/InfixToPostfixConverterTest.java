package com.example.equationsolver.parser;

import com.example.equationsolver.exception.InvalidEquationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfixToPostfixConverterTest {

    private InfixToPostfixConverter converter;

    @BeforeEach
    void setUp() {
        converter = new InfixToPostfixConverter(new Tokenizer());
    }

    private String postfix(String equation) {
        return InfixToPostfixConverter.format(converter.convert(equation));
    }

    @Test
    void convertsTheExampleFromTheAssignment() {
        assertEquals("3 x * 2 y * + z -", postfix("3x + 2y - z"));
    }

    @Test
    void powerBindsTighterThanPlusAndMinus() {
        assertEquals("x 2 ^ y 2 ^ + 4 -", postfix("x^2 + y^2 - 4"));
    }

    @Test
    void multiplicationBeforeAddition() {
        assertEquals("1 2 3 * +", postfix("1 + 2 * 3"));
    }

    @Test
    void sameLevelOperatorsGoLeftToRight() {
        assertEquals("10 4 - 3 -", postfix("10 - 4 - 3"));
        assertEquals("8 4 / 2 /", postfix("8 / 4 / 2"));
    }

    @Test
    void powerGoesRightToLeft() {
        assertEquals("2 3 2 ^ ^", postfix("2^3^2"));
    }

    @Test
    void bracketsChangeTheOrder() {
        assertEquals("1 2 + 3 *", postfix("(1 + 2) * 3"));
    }

    @Test
    void unaryMinusIsLowerThanPower() {
        assertEquals("x 2 ^ neg", postfix("-x^2"));
    }

    @Test
    void unaryMinusAfterAnOperator() {
        assertEquals("3 4 neg *", postfix("3 * -4"));
    }

    @Test
    void unaryPlusIsIgnored() {
        assertEquals("5", postfix("+5"));
    }

    @Test
    void implicitMultiplicationWithBrackets() {
        assertEquals("2 x 1 + *", postfix("2(x+1)"));
        assertEquals("x 1 + x 1 - *", postfix("(x+1)(x-1)"));
    }

    @Test
    void twoLettersNextToEachOtherAreMultiplied() {
        assertEquals("x y *", postfix("xy"));
    }

    @Test
    void decimalNumbers() {
        assertEquals("1.5 2 +", postfix("1.5 + 2"));
        assertEquals(".5 2 *", postfix(".5 * 2"));
    }

    @Test
    void emptyEquationIsRejected() {
        assertThrows(InvalidEquationException.class, () -> converter.convert(""));
        assertThrows(InvalidEquationException.class, () -> converter.convert("   "));
        assertThrows(InvalidEquationException.class, () -> converter.convert(null));
    }

    @Test
    void trailingOperatorIsRejected() {
        InvalidEquationException ex = assertThrows(InvalidEquationException.class, () -> converter.convert("3 +"));
        assertTrue(ex.getMessage().contains("incomplete"));
    }

    @Test
    void leadingMultiplyIsRejected() {
        InvalidEquationException ex = assertThrows(InvalidEquationException.class, () -> converter.convert("* 3"));
        assertTrue(ex.getMessage().contains("position 1"));
    }

    @Test
    void twoOperatorsInARowAreRejected() {
        assertThrows(InvalidEquationException.class, () -> converter.convert("3 + * 4"));
        assertThrows(InvalidEquationException.class, () -> converter.convert("3 ^ / 4"));
    }

    @Test
    void missingOperatorBetweenNumbersIsRejected() {
        InvalidEquationException ex = assertThrows(InvalidEquationException.class, () -> converter.convert("2 3"));
        assertTrue(ex.getMessage().contains("operator"));
    }

    @Test
    void unbalancedBracketsAreRejected() {
        assertThrows(InvalidEquationException.class, () -> converter.convert("(3 + 4"));
        assertThrows(InvalidEquationException.class, () -> converter.convert("3 + 4)"));
        assertThrows(InvalidEquationException.class, () -> converter.convert("()"));
        assertThrows(InvalidEquationException.class, () -> converter.convert("3 + (4 * )"));
    }

    @Test
    void unknownCharacterIsRejectedWithItsPosition() {
        InvalidEquationException ex = assertThrows(InvalidEquationException.class, () -> converter.convert("3 $ 4"));
        assertTrue(ex.getMessage().contains("'$'"));
        assertTrue(ex.getMessage().contains("position 3"));
    }

    @Test
    void equalsSignIsRejected() {
        assertThrows(InvalidEquationException.class, () -> converter.convert("x + 1 = 3"));
    }

    @Test
    void badNumbersAreRejected() {
        assertThrows(InvalidEquationException.class, () -> converter.convert("1.2.3 + 4"));
        assertThrows(InvalidEquationException.class, () -> converter.convert(". + 4"));
    }

    @Test
    void veryLongEquationIsRejected() {
        String longEquation = "1+".repeat(300) + "1";
        assertThrows(InvalidEquationException.class, () -> converter.convert(longEquation));
    }
}

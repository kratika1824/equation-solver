package com.example.equationsolver.tree;

import com.example.equationsolver.exception.EvaluationException;
import com.example.equationsolver.exception.InvalidEquationException;
import com.example.equationsolver.exception.MissingVariablesException;
import com.example.equationsolver.parser.InfixToPostfixConverter;
import com.example.equationsolver.parser.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionTreeTest {

    private final InfixToPostfixConverter converter = new InfixToPostfixConverter(new Tokenizer());
    private final ExpressionTreeBuilder builder = new ExpressionTreeBuilder();

    private ExprNode tree(String equation) {
        return builder.build(converter.convert(equation));
    }

    private double eval(String equation, Map<String, Double> variables) {
        return tree(equation).evaluate(variables);
    }

    // ---- structure of the tree ----

    @Test
    void operatorIsTheParentAndOperandsAreChildren() {
        ExprNode root = tree("a + b");
        assertTrue(root instanceof BinaryNode);
        BinaryNode plus = (BinaryNode) root;
        assertEquals(new VariableNode("a"), plus.left());
        assertEquals(new VariableNode("b"), plus.right());
    }

    @Test
    void lowestPrecedenceOperatorEndsUpAtTheRoot() {
        BinaryNode root = (BinaryNode) tree("1 + 2 * 3");
        assertEquals('+', root.operator().symbol());
        assertTrue(root.right() instanceof BinaryNode);
    }

    @Test
    void collectsEveryVariableOnce() {
        Set<String> found = new TreeSet<>();
        tree("x^2 + y^2 + x").collectVariables(found);
        assertEquals(Set.of("x", "y"), found);
    }

    // ---- evaluation ----

    @Test
    void evaluatesTheAssignmentExample() {
        double result = eval("3x + 2y - z", Map.of("x", 2.0, "y", 3.0, "z", 1.0));
        assertEquals(11.0, result, 0.0000001);
    }

    @Test
    void evaluatesPowers() {
        assertEquals(21.0,eval("x^2 + y^2 - 4", Map.of("x", 3.0, "y", 4.0)), 0.0000001);
    }

    @Test
    void powerIsRightAssociative() {
        assertEquals(512.0, eval("2^3^2", Map.of()), 0.0000001);
        assertEquals(64.0, eval("(2^3)^2", Map.of()), 0.0000001);
    }

    @Test
    void unaryMinusRules() {
        assertEquals(-9.0, eval("-x^2", Map.of("x", 3.0)), 0.0000001);
        assertEquals(9.0, eval("(-x)^2", Map.of("x", 3.0)), 0.0000001);
        assertEquals(7.0, eval("3 - -4", Map.of()), 0.0000001);
        assertEquals(-12.0, eval("3 * -4", Map.of()), 0.0000001);
    }

    @Test
    void evaluatesComplexNestedEquation() {
        // 2(x+1)(x-1) with x = 3 -> 2 * 4 * 2 = 16
        assertEquals(16.0, eval("2(x+1)(x-1)", Map.of("x", 3.0)), 0.0000001);
        // ((a + b) * (c - d)) / 2 with a=1 b=2 c=10 d=4 -> 9
        assertEquals(9.0, eval("((a + b) * (c - d)) / 2",
                Map.of("a", 1.0, "b", 2.0, "c", 10.0, "d", 4.0)), 0.0000001);
    }

    @Test
    void evaluatesDecimalsAndNegativeValues() {
        assertEquals(2.5, eval("10 / 4", Map.of()), 0.0000001);
        assertEquals(-4.5, eval("x * 1.5", Map.of("x", -3.0)), 0.0000001);
    }

    @Test
    void variablesAreCaseSensitive() {
        assertEquals(3.0, eval("x + X", Map.of("x", 1.0, "X", 2.0)), 0.0000001);
        assertThrows(MissingVariablesException.class, () -> eval("x + X", Map.of("x", 1.0)));
    }

    @Test
    void divisionByZeroIsReported() {
        EvaluationException ex = assertThrows(EvaluationException.class, () -> eval("1 / x", Map.of("x", 0.0)));
        assertEquals("Division by zero", ex.getMessage());
    }

    @Test
    void notARealNumberIsReported() {
        assertThrows(EvaluationException.class, () -> eval("(-8)^0.5", Map.of()));
    }

    @Test
    void zeroToANegativePowerIsReported() {
        assertThrows(EvaluationException.class, () -> eval("0^-1", Map.of()));
    }

    @Test
    void overflowIsReported() {
        assertThrows(EvaluationException.class, () -> eval("10^400", Map.of()));
    }

    @Test
    void hugeNumberLiteralIsRejectedWhenParsing() {
        String huge = "9".repeat(400);
        assertThrows(InvalidEquationException.class, () -> tree(huge));
    }

    // ---- rebuilding infix text ----

    @Test
    void rebuildsTheAssignmentExamples() {
        assertEquals("3x + 2y - z", tree("3x + 2y - z").toInfix());
        assertEquals("x^2 + y^2 - 4", tree("x^2 + y^2 - 4").toInfix());
    }

    @Test
    void normalisesSpacing() {
        assertEquals("3x + 2y - z", tree("3x+2y-z").toInfix());
        assertEquals("a + b * c", tree("a   +b*   c").toInfix());
    }

    @Test
    void keepsBracketsOnlyWhereTheyMatter() {
        assertEquals("(a + b) * c", tree("(a+b)*c").toInfix());
        assertEquals("a + b * c", tree("a+(b*c)").toInfix());
        assertEquals("a - (b - c)", tree("a-(b-c)").toInfix());
        assertEquals("a - b - c", tree("a-b-c").toInfix());
        assertEquals("a / b / c", tree("a/b/c").toInfix());
        assertEquals("1 / (2x)", tree("1/(2x)").toInfix());
    }

    @Test
    void rebuildsPowersAndNegation() {
        assertEquals("2^3^2", tree("2^3^2").toInfix());
        assertEquals("(2^3)^2", tree("(2^3)^2").toInfix());
        assertEquals("-x^2", tree("-x^2").toInfix());
        assertEquals("(-x)^2", tree("(-x)^2").toInfix());
        assertEquals("-(x + y)", tree("-(x+y)").toInfix());
        assertEquals("2x^2", tree("2x^2").toInfix());
    }

    @Test
    void rebuiltTextGivesTheSameAnswerWhenParsedAgain() {
        String[] equations = {"3x + 2y - z", "-(x+y)^2", "a/(b-c)*d", "2^-x", "(x+1)(x-1)"};
        Map<String, Double> values = Map.of("a", 5.0, "b", 3.0, "c", 1.0, "d", 2.0,
                "x", 2.0, "y", 3.0, "z", 4.0);
        for (String equation : equations) {
            ExprNode original = tree(equation);
            ExprNode again = tree(original.toInfix());
            assertEquals(original.evaluate(values), again.evaluate(values), 0.0000001, equation);
        }
    }
}

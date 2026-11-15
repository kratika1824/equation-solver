package com.example.equationsolver.service;

import com.example.equationsolver.exception.EquationNotFoundException;
import com.example.equationsolver.exception.EvaluationException;
import com.example.equationsolver.exception.InvalidEquationException;
import com.example.equationsolver.exception.InvalidRequestException;
import com.example.equationsolver.exception.MissingVariablesException;
import com.example.equationsolver.model.EvaluationResult;
import com.example.equationsolver.model.StoredEquation;
import com.example.equationsolver.parser.InfixToPostfixConverter;
import com.example.equationsolver.parser.Tokenizer;
import com.example.equationsolver.tree.ExpressionTreeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquationServiceTest {

    private EquationService service;

    @BeforeEach
    void setUp() {
        service = new EquationService(new InfixToPostfixConverter(new Tokenizer()), new ExpressionTreeBuilder());
    }

    @Test
    void storedEquationsGetIncreasingIds() {
        assertEquals(1L, service.store("3x + 2y - z").id());
        assertEquals(2L, service.store("x^2 + y^2 - 4").id());
    }

    @Test
    void storeKeepsThePostfixForm() {
        StoredEquation saved = service.store("3x + 2y - z");
        assertEquals("3 x * 2 y * + z -", saved.postfix());
    }

    @Test
    void invalidEquationIsNotStoredAndDoesNotUseUpAnId() {
        assertThrows(InvalidEquationException.class, () -> service.store("3 +"));
        assertTrue(service.findAll().isEmpty());
        assertEquals(1L, service.store("x + 1").id());
    }

    @Test
    void findAllReturnsEquationsInStorageOrder() {
        service.store("3x + 2y - z");
        service.store("x^2 + y^2 - 4");

        List<StoredEquation> all = service.findAll();
        assertEquals(2, all.size());
        assertEquals("3x + 2y - z", all.get(0).infix());
        assertEquals("x^2 + y^2 - 4", all.get(1).infix());
    }

    @Test
    void findAllOnEmptyStoreGivesEmptyList() {
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void evaluatesStoredEquation() {
        service.store("3x + 2y - z");
        EvaluationResult result = service.evaluate("1", Map.of("x", 2.0, "y", 3.0, "z", 1.0));
        assertEquals(11.0, result.value(), 0.0000001);
        assertEquals("3x + 2y - z", result.equation().infix());
    }

    @Test
    void sameEquationCanBeEvaluatedManyTimes() {
        service.store("x * x");
        assertEquals(4.0, service.evaluate("1", Map.of("x", 2.0)).value(), 0.0000001);
        assertEquals(9.0, service.evaluate("1", Map.of("x", 3.0)).value(), 0.0000001);
    }

    @Test
    void extraVariablesAreIgnored() {
        service.store("x + 1");
        EvaluationResult result = service.evaluate("1", Map.of("x", 1.0, "q", 50.0));
        assertEquals(2.0, result.value(), 0.0000001);
    }

    @Test
    void equationWithoutVariablesNeedsNoValues() {
        service.store("2 + 3 * 4");
        assertEquals(14.0, service.evaluate("1", null).value(), 0.0000001);
        assertEquals(14.0, service.evaluate("1", Map.of()).value(), 0.0000001);
    }

    @Test
    void missingVariablesAreListedByName() {
        service.store("3x + 2y - z");
        MissingVariablesException ex = assertThrows(MissingVariablesException.class,
                () -> service.evaluate("1", Map.of("x", 2.0)));
        assertEquals(List.of("y", "z"), ex.getMissing());
        assertTrue(ex.getMessage().contains("y, z"));
    }

    @Test
    void nullVariableValueIsRejected() {
        service.store("x + 1");
        Map<String, Double> variables = new HashMap<>();
        variables.put("x", null);
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> service.evaluate("1", variables));
        assertTrue(ex.getMessage().contains("'x'"));
    }

    @Test
    void badVariableNameIsRejected() {
        service.store("x + 1");
        assertThrows(InvalidRequestException.class, () -> service.evaluate("1", Map.of("xy", 1.0)));
        assertThrows(InvalidRequestException.class, () -> service.evaluate("1", Map.of("1", 1.0)));
    }

    @Test
    void unknownIdIsNotFound() {
        assertThrows(EquationNotFoundException.class, () -> service.evaluate("99", Map.of()));
    }

    @Test
    void nonNumericIdIsNotFound() {
        assertThrows(EquationNotFoundException.class, () -> service.findById("abc"));
        assertThrows(EquationNotFoundException.class, () -> service.findById(""));
    }

    @Test
    void divisionByZeroSurfacesFromEvaluate() {
        service.store("1 / x");
        assertThrows(EvaluationException.class, () -> service.evaluate("1", Map.of("x", 0.0)));
    }
}

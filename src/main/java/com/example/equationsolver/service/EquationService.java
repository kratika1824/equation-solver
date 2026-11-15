package com.example.equationsolver.service;

import com.example.equationsolver.exception.EquationNotFoundException;
import com.example.equationsolver.exception.InvalidRequestException;
import com.example.equationsolver.exception.MissingVariablesException;
import com.example.equationsolver.model.EvaluationResult;
import com.example.equationsolver.model.StoredEquation;
import com.example.equationsolver.parser.InfixToPostfixConverter;
import com.example.equationsolver.parser.Token;
import com.example.equationsolver.tree.ExprNode;
import com.example.equationsolver.tree.ExpressionTreeBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EquationService {

    private final InfixToPostfixConverter converter;
    private final ExpressionTreeBuilder treeBuilder;

    // sorted by id, and safe when several requests arrive at the same time
    private final Map<Long, StoredEquation> equations = new ConcurrentSkipListMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public EquationService(InfixToPostfixConverter converter, ExpressionTreeBuilder treeBuilder) {
        this.converter = converter;
        this.treeBuilder = treeBuilder;
    }

    public StoredEquation store(String equationText) {
        // parse first, so a bad equation never uses up an id
        List<Token> postfix = converter.convert(equationText);
        ExprNode root = treeBuilder.build(postfix);

        long id = idSequence.incrementAndGet();
        StoredEquation saved = new StoredEquation(id, root, InfixToPostfixConverter.format(postfix));
        equations.put(id, saved);
        return saved;
    }

    public List<StoredEquation> findAll() {
        return List.copyOf(equations.values());
    }

    public StoredEquation findById(String id) {
        try {
            StoredEquation found = equations.get(Long.parseLong(id.trim()));
            if (found != null) {
                return found;
            }
        } catch (NumberFormatException e) {
            // an id like "abc" can never exist, so it is reported as not found below
        }
        throw new EquationNotFoundException(id);
    }

    public EvaluationResult evaluate(String id, Map<String, Double> variables) {
        StoredEquation equation = findById(id);
        Map<String, Double> given = variables == null ? Map.of() : variables;

        checkVariables(given);

        Set<String> needed = new TreeSet<>();
        equation.root().collectVariables(needed);
        List<String> missing = needed.stream().filter(name -> !given.containsKey(name)).toList();
        if (!missing.isEmpty()) {
            throw new MissingVariablesException(missing);
        }

        double result = equation.root().evaluate(given);
        return new EvaluationResult(equation, given, result);
    }

    private void checkVariables(Map<String, Double> variables) {
        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            String name = entry.getKey();
            if (name == null || !name.matches("[A-Za-z]")) {
                throw new InvalidRequestException("Invalid variable name '" + name
                        + "': variables must be a single letter");
            }
            if (entry.getValue() == null) {
                throw new InvalidRequestException("Value for variable '" + name + "' must be a number");
            }
        }
    }
}

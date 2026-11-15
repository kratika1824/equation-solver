package com.example.equationsolver.model;

import java.util.Map;

public record EvaluationResult(StoredEquation equation, Map<String, Double> variables, double value) {
}

package com.example.equationsolver.dto;

import java.util.Map;

public record EvaluateResponse(String equationId, String equation, Map<String, Number> variables, Number result) {
}

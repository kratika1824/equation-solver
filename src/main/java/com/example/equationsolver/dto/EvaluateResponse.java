package com.example.equationsolver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record EvaluateResponse(
        @Schema(example = "1") String equationId,
        @Schema(example = "3x + 2y - z") String equation,
        @Schema(description = "The variable values that were used", example = "{\"x\": 2, \"y\": 3, \"z\": 1}")
        Map<String, Number> variables,
        @Schema(description = "Value of the equation for these variables", example = "11") Number result) {
}

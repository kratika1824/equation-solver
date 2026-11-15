package com.example.equationsolver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record EvaluateRequest(
        @Schema(description = "Value for each variable used in the equation",
                example = "{\"x\": 2, \"y\": 3, \"z\": 1}")
        Map<String, Double> variables) {
}

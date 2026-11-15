package com.example.equationsolver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EquationSummary(
        @Schema(example = "1") String equationId,
        @Schema(description = "Infix text rebuilt from the stored tree", example = "3x + 2y - z") String equation,
        @Schema(example = "3 x * 2 y * + z -") String postfix) {
}

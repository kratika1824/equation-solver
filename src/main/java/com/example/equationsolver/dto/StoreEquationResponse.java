package com.example.equationsolver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StoreEquationResponse(
        @Schema(example = "Equation stored successfully") String message,
        @Schema(description = "Id to use when evaluating this equation", example = "1") String equationId,
        @Schema(description = "The equation in postfix (Reverse Polish) order", example = "3 x * 2 y * + z -")
        String postfix) {
}

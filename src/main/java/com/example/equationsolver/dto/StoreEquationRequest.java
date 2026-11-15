package com.example.equationsolver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StoreEquationRequest(
        @Schema(description = "The equation to store. Variables are single letters.", example = "3x + 2y - z")
        String equation) {
}

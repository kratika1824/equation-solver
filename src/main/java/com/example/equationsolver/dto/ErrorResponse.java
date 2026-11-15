package com.example.equationsolver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "400") int status,
        @Schema(description = "Short error title", example = "Invalid equation") String error,
        @Schema(description = "What went wrong and where", example = "Unexpected character '$' at position 3")
        String message) {
}

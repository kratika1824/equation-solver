package com.example.equationsolver.dto;

public record ErrorResponse(int status, String error, String message) {
}

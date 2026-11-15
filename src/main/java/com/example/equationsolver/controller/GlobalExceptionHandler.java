package com.example.equationsolver.controller;

import com.example.equationsolver.dto.ErrorResponse;
import com.example.equationsolver.exception.EquationNotFoundException;
import com.example.equationsolver.exception.EvaluationException;
import com.example.equationsolver.exception.InvalidEquationException;
import com.example.equationsolver.exception.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns our exceptions into JSON error bodies with the right HTTP status. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidEquationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidEquation(InvalidEquationException ex) {
        return new ErrorResponse(400, "Invalid equation", ex.getMessage());
    }

    // also covers MissingVariablesException, which extends InvalidRequestException
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequest(InvalidRequestException ex) {
        return new ErrorResponse(400, "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException ex) {
        return new ErrorResponse(400, "Malformed request body",
                "Request body is missing, is not valid JSON, or has a value of the wrong type "
                        + "(variable values must be numbers)");
    }

    @ExceptionHandler(EquationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(EquationNotFoundException ex) {
        return new ErrorResponse(404, "Equation not found", ex.getMessage());
    }

    @ExceptionHandler(EvaluationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleEvaluationFailure(EvaluationException ex) {
        return new ErrorResponse(422, "Could not evaluate equation", ex.getMessage());
    }
}

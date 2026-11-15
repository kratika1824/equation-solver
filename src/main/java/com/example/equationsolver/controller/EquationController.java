package com.example.equationsolver.controller;

import com.example.equationsolver.dto.EquationListResponse;
import com.example.equationsolver.dto.EquationSummary;
import com.example.equationsolver.dto.EvaluateRequest;
import com.example.equationsolver.dto.EvaluateResponse;
import com.example.equationsolver.dto.StoreEquationRequest;
import com.example.equationsolver.dto.StoreEquationResponse;
import com.example.equationsolver.exception.InvalidEquationException;
import com.example.equationsolver.model.EvaluationResult;
import com.example.equationsolver.model.StoredEquation;
import com.example.equationsolver.service.EquationService;
import com.example.equationsolver.util.NumberFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equations")
@Tag(name = "Equations", description = "Store, list and evaluate algebraic equations")
public class EquationController {

    private final EquationService equationService;

    public EquationController(EquationService equationService) {
        this.equationService = equationService;
    }

    @PostMapping("/store")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Store an equation",
            description = "Parses the equation, converts it to postfix order and keeps it as an expression tree.")
    public StoreEquationResponse store(@RequestBody StoreEquationRequest request) {
        if (request == null || request.equation() == null) {
            throw new InvalidEquationException("Equation must not be empty");
        }
        StoredEquation saved = equationService.store(request.equation());
        return new StoreEquationResponse("Equation stored successfully", String.valueOf(saved.id()), saved.postfix());
    }

    @GetMapping
    @Operation(summary = "List stored equations",
            description = "Returns every stored equation, rebuilt as infix text by walking its tree.")
    public EquationListResponse list() {
        List<EquationSummary> summaries = equationService.findAll().stream()
                .map(e -> new EquationSummary(String.valueOf(e.id()), e.infix(), e.postfix()))
                .toList();
        return new EquationListResponse(summaries);
    }

    @PostMapping("/{equationId}/evaluate")
    @Operation(summary = "Evaluate an equation",
            description = "Substitutes the given variable values into the stored tree and returns the result.")
    public EvaluateResponse evaluate(@PathVariable String equationId, @RequestBody EvaluateRequest request) {
        EvaluationResult result = equationService.evaluate(equationId, request.variables());

        Map<String, Number> echoed = new LinkedHashMap<>();
        result.variables().forEach((name, value) -> echoed.put(name, NumberFormatter.toJsonNumber(value)));

        return new EvaluateResponse(
                String.valueOf(result.equation().id()),
                result.equation().infix(),
                echoed,
                NumberFormatter.toJsonNumber(result.value()));
    }
}

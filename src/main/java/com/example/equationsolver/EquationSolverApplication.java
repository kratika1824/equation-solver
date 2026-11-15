package com.example.equationsolver;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Algebraic Equation Solver API",
        version = "1.0.0",
        description = "Store algebraic equations as postfix (expression) trees, list them, "
                + "and evaluate them for given variable values. Storage is in memory."))
public class EquationSolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquationSolverApplication.class, args);
    }
}

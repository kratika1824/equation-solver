package com.example.equationsolver.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** Opening the bare URL sends you to the Swagger page. */
    @Operation(hidden = true)
    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}

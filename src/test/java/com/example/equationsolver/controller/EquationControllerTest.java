package com.example.equationsolver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full-stack tests: real controller, real service, real JSON, no mocks. */
@SpringBootTest
@AutoConfigureMockMvc
class EquationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ids are shared between tests (one Spring context), so each test reads the id it was given
    private String storeEquation(String equation) throws Exception {
        String body = mockMvc.perform(post("/api/equations/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equation\": \"" + equation + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("equationId").asText();
    }

    @Test
    void storeReturnsMessageIdAndPostfix() throws Exception {
        mockMvc.perform(post("/api/equations/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equation\": \"3x + 2y - z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Equation stored successfully"))
                .andExpect(jsonPath("$.equationId").isNotEmpty())
                .andExpect(jsonPath("$.postfix").value("3 x * 2 y * + z -"));
    }

    @Test
    void storeRejectsInvalidEquation() throws Exception {
        mockMvc.perform(post("/api/equations/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equation\": \"3 + * 4\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid equation"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void storeRejectsMissingEquationField() throws Exception {
        mockMvc.perform(post("/api/equations/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Equation must not be empty"));
    }

    @Test
    void storeRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/equations/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));
    }

    @Test
    void listShowsStoredEquationsAsInfix() throws Exception {
        storeEquation("x^2 + y^2 - 4");

        mockMvc.perform(get("/api/equations"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"equation\":\"x^2 + y^2 - 4\"")));
    }

    @Test
    void evaluateReturnsTheResult() throws Exception {
        String id = storeEquation("3x + 2y - z");

        mockMvc.perform(post("/api/equations/" + id + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {\"x\": 2, \"y\": 3, \"z\": 1}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equationId").value(id))
                .andExpect(jsonPath("$.equation").value("3x + 2y - z"))
                .andExpect(jsonPath("$.variables.x").value(2))
                .andExpect(jsonPath("$.result").value(11));
    }

    @Test
    void evaluateSupportsDecimalResults() throws Exception {
        String id = storeEquation("x / 4");

        mockMvc.perform(post("/api/equations/" + id + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {\"x\": 10}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2.5));
    }

    @Test
    void evaluateWithMissingVariableIsBadRequest() throws Exception {
        String id = storeEquation("3x + 2y - z");

        mockMvc.perform(post("/api/equations/" + id + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {\"x\": 2}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("y, z")));
    }

    @Test
    void evaluateWithNonNumericValueIsBadRequest() throws Exception {
        String id = storeEquation("x + 1");

        mockMvc.perform(post("/api/equations/" + id + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {\"x\": \"abc\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));
    }

    @Test
    void evaluateWithNullValueIsBadRequest() throws Exception {
        String id = storeEquation("x + 1");

        mockMvc.perform(post("/api/equations/" + id + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {\"x\": null}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("'x'")));
    }

    @Test
    void divisionByZeroIsUnprocessable() throws Exception {
        String id = storeEquation("1 / x");

        mockMvc.perform(post("/api/equations/" + id + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {\"x\": 0}}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Division by zero"));
    }

    @Test
    void unknownEquationIdIsNotFound() throws Exception {
        mockMvc.perform(post("/api/equations/9999/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\": {}}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Equation not found"));
    }

    @Test
    void swaggerJsonIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/equations/store")));
    }
}

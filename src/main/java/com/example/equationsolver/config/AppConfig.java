package com.example.equationsolver.config;

import com.example.equationsolver.parser.InfixToPostfixConverter;
import com.example.equationsolver.parser.Tokenizer;
import com.example.equationsolver.tree.ExpressionTreeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the plain (framework free) parsing classes as Spring beans. */
@Configuration
public class AppConfig {

    @Bean
    public InfixToPostfixConverter infixToPostfixConverter() {
        return new InfixToPostfixConverter(new Tokenizer());
    }

    @Bean
    public ExpressionTreeBuilder expressionTreeBuilder() {
        return new ExpressionTreeBuilder();
    }
}

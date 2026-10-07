package com.example.impact;

public record StepDefinitionDescriptor(
        String keyword,
        String pattern,
        String className,
        String methodName,
        String sourcePath,
        int line) {
}

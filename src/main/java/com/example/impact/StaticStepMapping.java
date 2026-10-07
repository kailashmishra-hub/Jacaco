package com.example.impact;

public record StaticStepMapping(
        CucumberScenario scenario,
        ScenarioStep step,
        StepDefinitionDescriptor stepDefinition) {
}

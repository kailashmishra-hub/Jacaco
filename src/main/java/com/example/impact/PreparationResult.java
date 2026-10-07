package com.example.impact;

import java.util.List;

public record PreparationResult(
        List<CucumberScenario> scenarios,
        List<StepDefinitionDescriptor> stepDefinitions,
        List<StaticStepMapping> mappings,
        List<UnmatchedScenarioStep> unmatchedSteps) {
}

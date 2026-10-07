package com.example.impact;

import java.util.ArrayList;
import java.util.List;

public class StaticStepMappingBuilder {
    public PreparationResult build(List<CucumberScenario> scenarios, List<StepDefinitionDescriptor> stepDefinitions) {
        List<StaticStepMapping> mappings = new ArrayList<>();
        List<UnmatchedScenarioStep> unmatched = new ArrayList<>();
        StepDefinitionMatcher matcher = new StepDefinitionMatcher();

        for (CucumberScenario scenario : scenarios) {
            for (ScenarioStep step : scenario.steps()) {
                StepDefinitionDescriptor definition = matcher.firstMatch(step, stepDefinitions);
                if (definition == null) {
                    unmatched.add(new UnmatchedScenarioStep(scenario, step));
                } else {
                    mappings.add(new StaticStepMapping(scenario, step, definition));
                }
            }
        }

        return new PreparationResult(scenarios, stepDefinitions, mappings, unmatched);
    }
}

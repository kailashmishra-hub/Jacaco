package com.example.impact;

import java.util.List;

public record CucumberScenario(
        String id,
        String featurePath,
        int line,
        String name,
        List<String> tags,
        List<ScenarioStep> steps) {
    public CucumberScenario {
        tags = List.copyOf(tags);
        steps = List.copyOf(steps);
    }
}

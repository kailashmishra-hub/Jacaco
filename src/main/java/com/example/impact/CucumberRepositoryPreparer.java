package com.example.impact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CucumberRepositoryPreparer {
    public PreparationResult prepare(Path repoRoot, Path outputDirectory) throws IOException {
        List<CucumberScenario> scenarios = new FeatureScenarioScanner().scan(repoRoot);
        List<StepDefinitionDescriptor> stepDefinitions = new StepDefinitionScanner().scan(repoRoot);
        PreparationResult result = new StaticStepMappingBuilder().build(scenarios, stepDefinitions);
        new PreparationWriter().write(result, outputDirectory);
        return result;
    }
}

package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PreparationWriter {
    public void write(PreparationResult result, Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        writeScenarioIndex(result, outputDirectory.resolve("scenario-index.csv"));
        writeStaticMap(result, outputDirectory.resolve("static-scenario-step-map.tsv"));
        writeUnmatched(result, outputDirectory.resolve("unmatched-steps.tsv"));
    }

    private void writeScenarioIndex(PreparationResult result, Path output) throws IOException {
        StringBuilder csv = new StringBuilder("scenarioId,featurePath,line,name,tags\n");
        for (CucumberScenario scenario : result.scenarios()) {
            csv.append(scenario.id()).append(',')
                    .append(scenario.featurePath()).append(',')
                    .append(scenario.line()).append(',')
                    .append(csvEscape(scenario.name())).append(',')
                    .append(csvEscape(String.join(" ", scenario.tags()))).append('\n');
        }
        Files.writeString(output, csv.toString(), StandardCharsets.UTF_8);
    }

    private void writeStaticMap(PreparationResult result, Path output) throws IOException {
        StringBuilder tsv = new StringBuilder(
                "scenarioId\tfeaturePath\tscenarioLine\tscenarioName\tstepLine\tstepKeyword\tstepText"
                        + "\tstepDefinitionClass\tstepDefinitionMethod\tstepDefinitionSource\tstepDefinitionLine\tpattern\n");
        for (StaticStepMapping mapping : result.mappings()) {
            CucumberScenario scenario = mapping.scenario();
            ScenarioStep step = mapping.step();
            StepDefinitionDescriptor definition = mapping.stepDefinition();
            tsv.append(scenario.id()).append('\t')
                    .append(scenario.featurePath()).append('\t')
                    .append(scenario.line()).append('\t')
                    .append(scenario.name()).append('\t')
                    .append(step.line()).append('\t')
                    .append(step.keyword()).append('\t')
                    .append(step.text()).append('\t')
                    .append(definition.className()).append('\t')
                    .append(definition.methodName()).append('\t')
                    .append(definition.sourcePath()).append('\t')
                    .append(definition.line()).append('\t')
                    .append(definition.pattern()).append('\n');
        }
        Files.writeString(output, tsv.toString(), StandardCharsets.UTF_8);
    }

    private void writeUnmatched(PreparationResult result, Path output) throws IOException {
        StringBuilder tsv = new StringBuilder(
                "scenarioId\tfeaturePath\tscenarioLine\tscenarioName\tstepLine\tstepKeyword\tstepText\n");
        for (UnmatchedScenarioStep unmatched : result.unmatchedSteps()) {
            CucumberScenario scenario = unmatched.scenario();
            ScenarioStep step = unmatched.step();
            tsv.append(scenario.id()).append('\t')
                    .append(scenario.featurePath()).append('\t')
                    .append(scenario.line()).append('\t')
                    .append(scenario.name()).append('\t')
                    .append(step.line()).append('\t')
                    .append(step.keyword()).append('\t')
                    .append(step.text()).append('\n');
        }
        Files.writeString(output, tsv.toString(), StandardCharsets.UTF_8);
    }

    private String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

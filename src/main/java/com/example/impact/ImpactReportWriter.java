package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

public class ImpactReportWriter {
    public void writeMarkdown(ImpactAnalysisResult result, Path output) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Impacted Cucumber Scenarios\n\n");

        if (result.impactedScenariosByMethod().isEmpty()) {
            markdown.append("No impacted scenarios were found for the supplied changes.\n");
        } else {
            result.impactedScenariosByMethod().forEach((methodId, scenarios) -> {
                markdown.append("## ").append(methodId).append("\n\n");
                for (ScenarioDescriptor scenario : scenarios) {
                    markdown.append("- ")
                            .append(scenario.featurePath())
                            .append(":")
                            .append(scenario.line())
                            .append(" - ")
                            .append(scenario.name());

                    if (!scenario.tags().isEmpty()) {
                        markdown.append(" ").append(formatTags(scenario));
                    }

                    markdown.append("\n");
                }
                markdown.append("\n");
            });
        }

        if (!result.unmappedChangedMethods().isEmpty()) {
            markdown.append("## Changed Methods Without Scenario Mapping\n\n");
            for (String methodId : result.unmappedChangedMethods()) {
                markdown.append("- ").append(methodId).append("\n");
            }
        }

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, markdown.toString(), StandardCharsets.UTF_8);
    }

    private String formatTags(ScenarioDescriptor scenario) {
        StringJoiner joiner = new StringJoiner(" ", "[", "]");
        scenario.tags().forEach(joiner::add);
        return joiner.toString();
    }
}

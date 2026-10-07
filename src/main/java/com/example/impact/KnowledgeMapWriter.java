package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.StringJoiner;

public class KnowledgeMapWriter {
    public void writeTsv(KnowledgeMap knowledgeMap, Path output) throws IOException {
        StringBuilder tsv = new StringBuilder("scenarioId\tfeaturePath\tline\tname\ttags\tmethodId\n");

        for (ScenarioDescriptor scenario : knowledgeMap.scenarios()) {
            Set<String> methodIds = knowledgeMap.methodIdsByScenarioId().getOrDefault(scenario.id(), Set.of());
            for (String methodId : methodIds) {
                tsv.append(scenario.id()).append('\t')
                        .append(scenario.featurePath()).append('\t')
                        .append(scenario.line()).append('\t')
                        .append(scenario.name()).append('\t')
                        .append(String.join(" ", scenario.tags())).append('\t')
                        .append(methodId).append('\n');
            }
        }

        write(output, tsv.toString());
    }

    public void writeScenarioMethodClassTsv(KnowledgeMap knowledgeMap, Path output) throws IOException {
        StringBuilder tsv = new StringBuilder(
                "scenarioId\tfeaturePath\tscenarioLine\tscenarioName\ttags\tclassName\tmethodName\tdescriptor\tmethodId\n");

        for (ScenarioDescriptor scenario : knowledgeMap.scenarios()) {
            Set<String> methodIds = knowledgeMap.methodIdsByScenarioId().getOrDefault(scenario.id(), Set.of());
            for (String methodId : methodIds) {
                CoveredMethod method = CoveredMethod.fromId(methodId);
                tsv.append(scenario.id()).append('\t')
                        .append(scenario.featurePath()).append('\t')
                        .append(scenario.line()).append('\t')
                        .append(scenario.name()).append('\t')
                        .append(String.join(" ", scenario.tags())).append('\t')
                        .append(method.className()).append('\t')
                        .append(method.methodName()).append('\t')
                        .append(method.descriptor()).append('\t')
                        .append(method.id()).append('\n');
            }
        }

        write(output, tsv.toString());
    }

    public void writeJson(KnowledgeMap knowledgeMap, Path output) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"version\": 1,\n  \"scenarios\": [\n");

        int scenarioIndex = 0;
        for (ScenarioDescriptor scenario : knowledgeMap.scenarios()) {
            if (scenarioIndex++ > 0) {
                json.append(",\n");
            }
            json.append("    {")
                    .append("\"id\": \"").append(escape(scenario.id())).append("\", ")
                    .append("\"featurePath\": \"").append(escape(scenario.featurePath())).append("\", ")
                    .append("\"line\": ").append(scenario.line()).append(", ")
                    .append("\"name\": \"").append(escape(scenario.name())).append("\", ")
                    .append("\"tags\": ").append(tagsJson(scenario)).append("}");
        }

        json.append("\n  ],\n  \"coverage\": {\n");
        int methodIndex = 0;
        for (var entry : knowledgeMap.scenarioIdsByMethodId().entrySet()) {
            if (methodIndex++ > 0) {
                json.append(",\n");
            }
            json.append("    \"").append(escape(entry.getKey())).append("\": [");
            StringJoiner joiner = new StringJoiner(", ");
            entry.getValue().forEach(scenarioId -> joiner.add("\"" + escape(scenarioId) + "\""));
            json.append(joiner).append("]");
        }

        json.append("\n  }\n}\n");
        write(output, json.toString());
    }

    private void write(Path output, String content) throws IOException {
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, content, StandardCharsets.UTF_8);
    }

    private String tagsJson(ScenarioDescriptor scenario) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        scenario.tags().forEach(tag -> joiner.add("\"" + escape(tag) + "\""));
        return joiner.toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

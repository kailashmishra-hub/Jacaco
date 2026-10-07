package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScenarioIndexReader {
    public List<ScenarioDescriptor> read(Path scenarioIndex) throws IOException {
        List<String> lines = Files.readAllLines(scenarioIndex, StandardCharsets.UTF_8);
        List<ScenarioDescriptor> scenarios = new ArrayList<>();

        for (String line : lines.stream().skip(1).toList()) {
            if (line.isBlank()) {
                continue;
            }

            List<String> columns = parseCsvLine(line);
            if (columns.size() < 5) {
                throw new IOException("Scenario index row must have 5 columns: " + line);
            }

            scenarios.add(new ScenarioDescriptor(
                    columns.get(0),
                    columns.get(1),
                    Integer.parseInt(columns.get(2)),
                    columns.get(3),
                    ScenarioDescriptor.parseTags(columns.get(4))));
        }

        return scenarios;
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder column = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                quoted = !quoted;
            } else if (current == ',' && !quoted) {
                columns.add(column.toString().trim());
                column.setLength(0);
            } else {
                column.append(current);
            }
        }

        columns.add(column.toString().trim());
        return columns;
    }
}

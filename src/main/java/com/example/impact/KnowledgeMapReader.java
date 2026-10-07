package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class KnowledgeMapReader {
    public KnowledgeMap readTsv(Path path) throws IOException {
        KnowledgeMap knowledgeMap = new KnowledgeMap();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (String line : lines.stream().skip(1).toList()) {
            String[] columns = line.split("\t", -1);
            if (columns.length < 6) {
                continue;
            }

            ScenarioDescriptor scenario = new ScenarioDescriptor(
                    columns[0],
                    columns[1],
                    Integer.parseInt(columns[2]),
                    columns[3],
                    ScenarioDescriptor.parseTags(columns[4]));
            knowledgeMap.addScenario(scenario);
            knowledgeMap.addCoverage(scenario.id(), CoveredMethod.fromId(columns[5]));
        }

        return knowledgeMap;
    }
}

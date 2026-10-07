package com.example.impact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class KnowledgeMapBuilder {
    private final JacocoXmlCoverageReader coverageReader;

    public KnowledgeMapBuilder(JacocoXmlCoverageReader coverageReader) {
        this.coverageReader = coverageReader;
    }

    public KnowledgeMap build(List<ScenarioDescriptor> scenarios, Path coverageDirectory) throws IOException {
        KnowledgeMap knowledgeMap = new KnowledgeMap();

        for (ScenarioDescriptor scenario : scenarios) {
            knowledgeMap.addScenario(scenario);
            Path report = coverageDirectory.resolve(scenario.id() + ".xml");
            if (!Files.exists(report)) {
                throw new IOException("Missing JaCoCo report for scenario " + scenario.id() + ": " + report);
            }

            for (CoveredMethod method : coverageReader.readCoveredMethods(report)) {
                knowledgeMap.addCoverage(scenario.id(), method);
            }
        }

        return knowledgeMap;
    }
}

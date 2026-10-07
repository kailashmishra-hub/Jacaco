package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImpactTrackerApp {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "build" -> build(args);
            case "impact" -> impact(args);
            default -> {
                System.err.println("Unknown command: " + args[0]);
                printUsage();
                System.exit(2);
            }
        }
    }

    private static void build(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        Path scenarioIndex = options.required("--scenario-index");
        Path coverageDirectory = options.required("--coverage-dir");
        Path outputDirectory = options.required("--output-dir");

        List<ScenarioDescriptor> scenarios = new ScenarioIndexReader().read(scenarioIndex);
        KnowledgeMap knowledgeMap = new KnowledgeMapBuilder(new JacocoXmlCoverageReader())
                .build(scenarios, coverageDirectory);

        KnowledgeMapWriter writer = new KnowledgeMapWriter();
        writer.writeJson(knowledgeMap, outputDirectory.resolve("knowledge-map.json"));
        writer.writeTsv(knowledgeMap, outputDirectory.resolve("scenario-method-map.tsv"));
    }

    private static void impact(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        Path map = options.required("--map");
        Path changedMethods = options.required("--changed-methods");
        Path output = options.required("--output");

        KnowledgeMap knowledgeMap = new KnowledgeMapReader().readTsv(map);
        ImpactAnalysisResult result = new ImpactAnalyzer().analyze(knowledgeMap, readChangedMethods(changedMethods));
        new ImpactReportWriter().writeMarkdown(result, output);
    }

    private static List<String> readChangedMethods(Path changedMethods) throws IOException {
        return Files.readAllLines(changedMethods, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .toList();
    }

    private static void printUsage() {
        System.out.println("""
                Usage:
                  java -jar cucumber-impact-tracker.jar build --scenario-index <csv> --coverage-dir <dir> --output-dir <dir>
                  java -jar cucumber-impact-tracker.jar impact --map <scenario-method-map.tsv> --changed-methods <txt> --output <md>

                Scenario index CSV columns:
                  scenarioId,featurePath,line,name,tags

                Each scenarioId must have a matching JaCoCo XML file:
                  <coverage-dir>/<scenarioId>.xml
                """);
    }
}

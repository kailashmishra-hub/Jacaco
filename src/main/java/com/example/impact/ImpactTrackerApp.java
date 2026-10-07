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
            case "graph" -> graph(args);
            case "compare-graph" -> compareGraph(args);
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
        writer.writeScenarioMethodClassTsv(knowledgeMap, outputDirectory.resolve("scenario-class-method-map.tsv"));
    }

    private static void graph(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        Path jacocoXml = options.required("--jacoco-xml");
        Path outputDirectory = options.required("--output-dir");

        List<MethodGraphNode> nodes = new JacocoMethodGraphReader().read(jacocoXml);
        new MethodGraphTsv().write(nodes, outputDirectory.resolve("method-graph.tsv"));

        MethodGraphWriter writer = new MethodGraphWriter();
        writer.writeDot(nodes, outputDirectory.resolve("method-graph.dot"));
        writer.writeMarkdownIndex(nodes, outputDirectory.resolve("method-index.md"));
    }

    private static void compareGraph(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        Path baseline = options.required("--baseline");
        Path current = options.required("--current");
        Path output = options.required("--output");

        MethodGraphTsv tsv = new MethodGraphTsv();
        List<MethodGraphChange> changes = new MethodGraphComparator().compare(
                tsv.read(baseline),
                tsv.read(current));
        new MethodGraphReportWriter().writeMarkdown(changes, output);
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
                  java -jar cucumber-impact-tracker.jar graph --jacoco-xml <jacoco.xml> --output-dir <dir>
                  java -jar cucumber-impact-tracker.jar compare-graph --baseline <method-graph.tsv> --current <method-graph.tsv> --output <md>
                  java -jar cucumber-impact-tracker.jar impact --map <scenario-method-map.tsv> --changed-methods <txt> --output <md>

                Scenario index CSV columns:
                  scenarioId,featurePath,line,name,tags

                Each scenarioId must have a matching JaCoCo XML file:
                  <coverage-dir>/<scenarioId>.xml
                """);
    }
}

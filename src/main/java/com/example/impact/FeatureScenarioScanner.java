package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FeatureScenarioScanner {
    public List<CucumberScenario> scan(Path repoRoot) throws IOException {
        List<Path> featureFiles;
        try (var paths = Files.walk(repoRoot)) {
            featureFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".feature"))
                    .filter(path -> !isGeneratedPath(repoRoot, path))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }

        List<CucumberScenario> scenarios = new ArrayList<>();
        for (Path featureFile : featureFiles) {
            scenarios.addAll(scanFeature(repoRoot, featureFile));
        }
        return scenarios;
    }

    private List<CucumberScenario> scanFeature(Path repoRoot, Path featureFile) throws IOException {
        List<String> lines = Files.readAllLines(featureFile, StandardCharsets.UTF_8);
        String featurePath = normalize(repoRoot.relativize(featureFile));
        List<CucumberScenario> scenarios = new ArrayList<>();
        List<String> pendingTags = new ArrayList<>();
        List<ScenarioStep> backgroundSteps = new ArrayList<>();
        List<ScenarioStep> currentSteps = new ArrayList<>();
        String currentName = null;
        int currentLine = -1;
        boolean inBackground = false;

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String trimmed = lines.get(index).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            if (trimmed.startsWith("@")) {
                pendingTags = ScenarioDescriptor.parseTags(trimmed);
                continue;
            }

            if (startsWithKeyword(trimmed, "Background:")) {
                inBackground = true;
                currentName = null;
                currentSteps = new ArrayList<>();
                continue;
            }

            if (startsWithKeyword(trimmed, "Scenario:") || startsWithKeyword(trimmed, "Scenario Outline:")) {
                if (currentName != null) {
                    scenarios.add(scenario(featurePath, currentLine, currentName, pendingTags, currentSteps));
                }
                inBackground = false;
                currentLine = lineNumber;
                currentName = trimmed.substring(trimmed.indexOf(':') + 1).trim();
                currentSteps = new ArrayList<>(backgroundSteps);
                pendingTags = new ArrayList<>(pendingTags);
                continue;
            }

            ScenarioStep step = parseStep(trimmed, lineNumber);
            if (step != null) {
                if (inBackground) {
                    backgroundSteps.add(step);
                } else if (currentName != null) {
                    currentSteps.add(step);
                }
            }
        }

        if (currentName != null) {
            scenarios.add(scenario(featurePath, currentLine, currentName, pendingTags, currentSteps));
        }

        return scenarios;
    }

    private CucumberScenario scenario(
            String featurePath,
            int line,
            String name,
            List<String> tags,
            List<ScenarioStep> steps) {
        return new CucumberScenario(scenarioId(featurePath, line, name), featurePath, line, name, tags, steps);
    }

    private ScenarioStep parseStep(String trimmed, int line) {
        for (String keyword : List.of("Given", "When", "Then", "And", "But", "*")) {
            String prefix = keyword + " ";
            if (trimmed.startsWith(prefix)) {
                return new ScenarioStep(keyword, trimmed.substring(prefix.length()).trim(), line);
            }
        }
        return null;
    }

    private boolean startsWithKeyword(String value, String keyword) {
        return value.regionMatches(true, 0, keyword, 0, keyword.length());
    }

    private String scenarioId(String featurePath, int line, String name) {
        String base = featurePath + "-" + line + "-" + name;
        return base.toLowerCase(Locale.ROOT)
                .replace('\\', '-')
                .replace('/', '-')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private boolean isGeneratedPath(Path repoRoot, Path path) {
        String normalized = normalize(repoRoot.relativize(path));
        return normalized.contains("/target/")
                || normalized.startsWith("target/")
                || normalized.contains("/build/")
                || normalized.startsWith("build/");
    }

    private String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }
}

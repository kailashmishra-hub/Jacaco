package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepDefinitionScanner {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+(\\w+)");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile(
            "@(Given|When|Then|And|But)\\s*\\(\\s*(?:value\\s*=\\s*)?\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\b(?:public|protected|private)?\\s*(?:static\\s+)?[\\w<>\\[\\], ?]+\\s+(\\w+)\\s*\\(");

    public List<StepDefinitionDescriptor> scan(Path repoRoot) throws IOException {
        List<Path> javaFiles;
        try (var paths = Files.walk(repoRoot)) {
            javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isGeneratedPath(repoRoot, path))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }

        List<StepDefinitionDescriptor> definitions = new ArrayList<>();
        for (Path javaFile : javaFiles) {
            definitions.addAll(scanJavaFile(repoRoot, javaFile));
        }
        return definitions;
    }

    private List<StepDefinitionDescriptor> scanJavaFile(Path repoRoot, Path javaFile) throws IOException {
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
        String packageName = "";
        String className = javaFile.getFileName().toString().replace(".java", "");
        List<StepDefinitionDescriptor> definitions = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            Matcher packageMatcher = PACKAGE_PATTERN.matcher(line);
            if (packageMatcher.find()) {
                packageName = packageMatcher.group(1);
            }

            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                className = classMatcher.group(1);
            }

            Matcher annotationMatcher = ANNOTATION_PATTERN.matcher(line);
            if (annotationMatcher.find()) {
                String methodName = findMethodName(lines, index + 1);
                if (methodName != null) {
                    definitions.add(new StepDefinitionDescriptor(
                            annotationMatcher.group(1),
                            unescapeJavaString(annotationMatcher.group(2)),
                            packageName.isBlank() ? className : packageName + "." + className,
                            methodName,
                            normalize(repoRoot.relativize(javaFile)),
                            index + 1));
                }
            }
        }

        return definitions;
    }

    private String findMethodName(List<String> lines, int startIndex) {
        for (int index = startIndex; index < Math.min(lines.size(), startIndex + 8); index++) {
            Matcher matcher = METHOD_PATTERN.matcher(lines.get(index));
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String unescapeJavaString(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
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

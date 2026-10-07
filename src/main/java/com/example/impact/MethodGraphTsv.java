package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MethodGraphTsv {
    private static final String HEADER = String.join("\t",
            "packageName",
            "className",
            "sourceFile",
            "methodName",
            "descriptor",
            "line",
            "instruction",
            "branch",
            "lineCounter",
            "complexity",
            "methodCounter");

    public void write(List<MethodGraphNode> nodes, Path output) throws IOException {
        StringBuilder tsv = new StringBuilder(HEADER).append('\n');
        for (MethodGraphNode node : nodes) {
            tsv.append(node.packageName()).append('\t')
                    .append(node.className()).append('\t')
                    .append(node.sourceFile()).append('\t')
                    .append(node.methodName()).append('\t')
                    .append(node.descriptor()).append('\t')
                    .append(node.line()).append('\t')
                    .append(node.instructionCounter().compact()).append('\t')
                    .append(node.branchCounter().compact()).append('\t')
                    .append(node.lineCounter().compact()).append('\t')
                    .append(node.complexityCounter().compact()).append('\t')
                    .append(node.methodCounter().compact()).append('\n');
        }

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, tsv.toString(), StandardCharsets.UTF_8);
    }

    public List<MethodGraphNode> read(Path input) throws IOException {
        List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
        List<MethodGraphNode> nodes = new ArrayList<>();

        for (String line : lines.stream().skip(1).toList()) {
            if (line.isBlank()) {
                continue;
            }
            String[] columns = line.split("\t", -1);
            if (columns.length < 11) {
                throw new IOException("Method graph row must have 11 columns: " + line);
            }
            nodes.add(new MethodGraphNode(
                    columns[0],
                    columns[1],
                    columns[2],
                    columns[3],
                    columns[4],
                    Integer.parseInt(columns[5]),
                    CoverageCounter.parse(columns[6]),
                    CoverageCounter.parse(columns[7]),
                    CoverageCounter.parse(columns[8]),
                    CoverageCounter.parse(columns[9]),
                    CoverageCounter.parse(columns[10])));
        }

        return nodes;
    }
}

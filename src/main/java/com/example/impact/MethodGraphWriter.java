package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MethodGraphWriter {
    public void writeDot(List<MethodGraphNode> nodes, Path output) throws IOException {
        StringBuilder dot = new StringBuilder("digraph jacoco_method_graph {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=box];\n");

        for (MethodGraphNode node : nodes) {
            String classId = id("class_" + node.className());
            String methodId = id("method_" + node.methodId());
            dot.append("  ").append(classId)
                    .append(" [label=\"")
                    .append(escape(node.className()))
                    .append("\"];\n");
            dot.append("  ").append(methodId)
                    .append(" [label=\"")
                    .append(escape(node.methodName() + node.descriptor()))
                    .append("\\nline ")
                    .append(node.line())
                    .append("\\n")
                    .append(node.coverageFingerprint())
                    .append("\"];\n");
            dot.append("  ").append(classId).append(" -> ").append(methodId).append(";\n");
        }

        dot.append("}\n");
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, dot.toString(), StandardCharsets.UTF_8);
    }

    public void writeMarkdownIndex(List<MethodGraphNode> nodes, Path output) throws IOException {
        StringBuilder markdown = new StringBuilder("# JaCoCo Method Knowledge Graph\n\n");
        for (MethodGraphNode node : nodes) {
            markdown.append("- `")
                    .append(node.methodId())
                    .append("`")
                    .append(" from ")
                    .append(node.sourceFile())
                    .append(":")
                    .append(node.line())
                    .append(" - ")
                    .append(node.coverageFingerprint())
                    .append("\n");
        }

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, markdown.toString(), StandardCharsets.UTF_8);
    }

    private String id(String value) {
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

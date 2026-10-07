package com.example.impact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MethodGraphReportWriter {
    public void writeMarkdown(List<MethodGraphChange> changes, Path output) throws IOException {
        StringBuilder markdown = new StringBuilder("# Method Knowledge Graph Changes\n\n");

        if (changes.isEmpty()) {
            markdown.append("No method-level graph changes were detected.\n");
        } else {
            for (MethodGraphChange change : changes) {
                markdown.append("## ")
                        .append(change.type())
                        .append(": ")
                        .append(change.methodId())
                        .append("\n\n");

                if (change.before() != null) {
                    markdown.append("- Before: ")
                            .append(change.before().coverageFingerprint())
                            .append("\n");
                }
                if (change.after() != null) {
                    markdown.append("- After: ")
                            .append(change.after().coverageFingerprint())
                            .append("\n");
                    markdown.append("- Source: ")
                            .append(change.after().sourceFile())
                            .append(":")
                            .append(change.after().line())
                            .append("\n");
                }
                markdown.append("\n");
            }
        }

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, markdown.toString(), StandardCharsets.UTF_8);
    }
}

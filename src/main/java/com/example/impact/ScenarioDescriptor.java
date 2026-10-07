package com.example.impact;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record ScenarioDescriptor(String id, String featurePath, int line, String name, List<String> tags) {
    public ScenarioDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(featurePath, "featurePath");
        Objects.requireNonNull(name, "name");
        tags = List.copyOf(tags);
    }

    public static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.trim().split("\\s+"))
                .filter(tag -> !tag.isBlank())
                .toList();
    }
}

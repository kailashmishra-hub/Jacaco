package com.example.impact;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

class CliOptions {
    private final Map<String, String> values;

    private CliOptions(Map<String, String> values) {
        this.values = values;
    }

    static CliOptions parse(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 1; index < args.length; index += 2) {
            if (!args[index].startsWith("--") || index + 1 >= args.length) {
                throw new IllegalArgumentException("Expected option/value pair near: " + args[index]);
            }
            values.put(args[index], args[index + 1]);
        }
        return new CliOptions(values);
    }

    Path required(String name) {
        String value = values.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required option " + name);
        }
        return Path.of(value);
    }
}

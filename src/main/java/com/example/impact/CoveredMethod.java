package com.example.impact;

import java.util.Objects;

public record CoveredMethod(String className, String methodName, String descriptor, int line) {
    public String id() {
        return className + "#" + methodName + descriptor;
    }

    public static CoveredMethod fromId(String id) {
        int separator = id.indexOf('#');
        int descriptorStart = id.indexOf('(', separator + 1);

        if (separator < 1 || descriptorStart < 0) {
            throw new IllegalArgumentException("Method id must look like com.acme.Type#method(Descriptor): " + id);
        }

        return new CoveredMethod(
                id.substring(0, separator),
                id.substring(separator + 1, descriptorStart),
                id.substring(descriptorStart),
                -1);
    }

    public CoveredMethod {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(descriptor, "descriptor");
    }
}

package com.example.impact;

public record MethodGraphChange(ChangeType type, MethodGraphNode before, MethodGraphNode after) {
    public String methodId() {
        MethodGraphNode node = after != null ? after : before;
        return node.methodId();
    }

    public enum ChangeType {
        ADDED,
        REMOVED,
        COVERAGE_CHANGED
    }
}

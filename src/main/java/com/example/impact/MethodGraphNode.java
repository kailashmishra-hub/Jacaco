package com.example.impact;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MethodGraphNode(
        String packageName,
        String className,
        String sourceFile,
        String methodName,
        String descriptor,
        int line,
        CoverageCounter instructionCounter,
        CoverageCounter branchCounter,
        CoverageCounter lineCounter,
        CoverageCounter complexityCounter,
        CoverageCounter methodCounter) {
    public String methodId() {
        return className + "#" + methodName + descriptor;
    }

    public String coverageFingerprint() {
        return "instruction=" + instructionCounter.compact()
                + ";branch=" + branchCounter.compact()
                + ";line=" + lineCounter.compact()
                + ";complexity=" + complexityCounter.compact()
                + ";method=" + methodCounter.compact();
    }

    public static Map<String, MethodGraphNode> byId(List<MethodGraphNode> nodes) {
        Map<String, MethodGraphNode> byId = new LinkedHashMap<>();
        for (MethodGraphNode node : nodes) {
            byId.put(node.methodId(), node);
        }
        return byId;
    }
}

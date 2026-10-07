package com.example.impact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodGraphComparator {
    public List<MethodGraphChange> compare(List<MethodGraphNode> baseline, List<MethodGraphNode> current) {
        Map<String, MethodGraphNode> baselineById = MethodGraphNode.byId(baseline);
        Map<String, MethodGraphNode> currentById = MethodGraphNode.byId(current);
        List<MethodGraphChange> changes = new ArrayList<>();

        for (MethodGraphNode currentNode : current) {
            MethodGraphNode baselineNode = baselineById.get(currentNode.methodId());
            if (baselineNode == null) {
                changes.add(new MethodGraphChange(MethodGraphChange.ChangeType.ADDED, null, currentNode));
            } else if (!baselineNode.coverageFingerprint().equals(currentNode.coverageFingerprint())) {
                changes.add(new MethodGraphChange(
                        MethodGraphChange.ChangeType.COVERAGE_CHANGED,
                        baselineNode,
                        currentNode));
            }
        }

        for (MethodGraphNode baselineNode : baseline) {
            if (!currentById.containsKey(baselineNode.methodId())) {
                changes.add(new MethodGraphChange(MethodGraphChange.ChangeType.REMOVED, baselineNode, null));
            }
        }

        return changes;
    }
}

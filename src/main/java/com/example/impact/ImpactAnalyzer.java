package com.example.impact;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImpactAnalyzer {
    public ImpactAnalysisResult analyze(KnowledgeMap knowledgeMap, List<String> changedMethodIds) {
        Map<String, List<ScenarioDescriptor>> impacted = new LinkedHashMap<>();
        List<String> unmapped = new ArrayList<>();

        for (String changedMethodId : changedMethodIds) {
            List<ScenarioDescriptor> scenarios = knowledgeMap.scenariosCovering(changedMethodId);
            if (scenarios.isEmpty()) {
                unmapped.add(changedMethodId);
            } else {
                impacted.put(changedMethodId, scenarios);
            }
        }

        return new ImpactAnalysisResult(impacted, unmapped);
    }
}

package com.example.impact;

import java.util.List;
import java.util.Map;

public record ImpactAnalysisResult(
        Map<String, List<ScenarioDescriptor>> impactedScenariosByMethod,
        List<String> unmappedChangedMethods) {
}

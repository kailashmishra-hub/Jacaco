package com.example.impact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KnowledgeMap {
    private final Map<String, ScenarioDescriptor> scenariosById = new LinkedHashMap<>();
    private final Map<String, Set<String>> scenarioIdsByMethodId = new LinkedHashMap<>();
    private final Map<String, Set<String>> methodIdsByScenarioId = new LinkedHashMap<>();

    public void addScenario(ScenarioDescriptor scenario) {
        scenariosById.put(scenario.id(), scenario);
    }

    public void addCoverage(String scenarioId, CoveredMethod method) {
        scenarioIdsByMethodId.computeIfAbsent(method.id(), ignored -> new LinkedHashSet<>()).add(scenarioId);
        methodIdsByScenarioId.computeIfAbsent(scenarioId, ignored -> new LinkedHashSet<>()).add(method.id());
    }

    public Collection<ScenarioDescriptor> scenarios() {
        return scenariosById.values();
    }

    public Map<String, Set<String>> scenarioIdsByMethodId() {
        return scenarioIdsByMethodId;
    }

    public Map<String, Set<String>> methodIdsByScenarioId() {
        return methodIdsByScenarioId;
    }

    public List<ScenarioDescriptor> scenariosCovering(String methodId) {
        Set<String> scenarioIds = scenarioIdsByMethodId.getOrDefault(methodId, Set.of());
        List<ScenarioDescriptor> scenarios = new ArrayList<>();
        for (String scenarioId : scenarioIds) {
            ScenarioDescriptor scenario = scenariosById.get(scenarioId);
            if (scenario != null) {
                scenarios.add(scenario);
            }
        }
        return scenarios;
    }
}

package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class MethodGraphComparatorTest {
    @Test
    void reportsAddedRemovedAndCoverageChangedMethods() {
        MethodGraphNode unchanged = node("com.acme.Login#unchanged()V", new CoverageCounter(0, 4));
        MethodGraphNode changedBefore = node("com.acme.Login#changed()V", new CoverageCounter(2, 2));
        MethodGraphNode changedAfter = node("com.acme.Login#changed()V", new CoverageCounter(0, 4));
        MethodGraphNode removed = node("com.acme.Login#removed()V", new CoverageCounter(0, 1));
        MethodGraphNode added = node("com.acme.Login#added()V", new CoverageCounter(0, 1));

        List<MethodGraphChange> changes = new MethodGraphComparator().compare(
                List.of(unchanged, changedBefore, removed),
                List.of(unchanged, changedAfter, added));

        assertEquals(MethodGraphChange.ChangeType.COVERAGE_CHANGED, changes.get(0).type());
        assertEquals("com.acme.Login#changed()V", changes.get(0).methodId());
        assertEquals(MethodGraphChange.ChangeType.ADDED, changes.get(1).type());
        assertEquals("com.acme.Login#added()V", changes.get(1).methodId());
        assertEquals(MethodGraphChange.ChangeType.REMOVED, changes.get(2).type());
        assertEquals("com.acme.Login#removed()V", changes.get(2).methodId());
    }

    private MethodGraphNode node(String id, CoverageCounter instructionCounter) {
        CoveredMethod method = CoveredMethod.fromId(id);
        return new MethodGraphNode(
                "com.acme",
                method.className(),
                "Login.java",
                method.methodName(),
                method.descriptor(),
                10,
                instructionCounter,
                new CoverageCounter(0, 0),
                new CoverageCounter(0, 1),
                new CoverageCounter(0, 1),
                new CoverageCounter(0, 1));
    }
}

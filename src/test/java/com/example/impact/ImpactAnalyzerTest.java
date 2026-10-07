package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ImpactAnalyzerTest {
    @Test
    void returnsScenariosImpactedByChangedMethods() {
        ScenarioDescriptor checkoutScenario = new ScenarioDescriptor(
                "checkout-success",
                "features/checkout.feature",
                12,
                "Successful checkout",
                List.of("@smoke"));
        KnowledgeMap knowledgeMap = new KnowledgeMap();
        knowledgeMap.addScenario(checkoutScenario);
        knowledgeMap.addCoverage(
                checkoutScenario.id(),
                new CoveredMethod("com.acme.steps.CheckoutSteps", "submitOrder", "()V", 42));

        ImpactAnalysisResult result = new ImpactAnalyzer().analyze(
                knowledgeMap,
                List.of("com.acme.steps.CheckoutSteps#submitOrder()V", "com.acme.helpers.PaymentHelper#refund()V"));

        assertEquals(List.of(checkoutScenario), result.impactedScenariosByMethod()
                .get("com.acme.steps.CheckoutSteps#submitOrder()V"));
        assertEquals(List.of("com.acme.helpers.PaymentHelper#refund()V"), result.unmappedChangedMethods());
    }
}

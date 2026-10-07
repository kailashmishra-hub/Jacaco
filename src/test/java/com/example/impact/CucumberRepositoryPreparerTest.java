package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CucumberRepositoryPreparerTest {
    @TempDir
    Path tempDirectory;

    @Test
    void preparesScenarioIndexAndStaticStepMap() throws Exception {
        Path repo = tempDirectory.resolve("repo");
        Path featureDirectory = repo.resolve("src/test/resources/features");
        Path stepDirectory = repo.resolve("src/test/java/com/acme/steps");
        Files.createDirectories(featureDirectory);
        Files.createDirectories(stepDirectory);

        Files.writeString(featureDirectory.resolve("login.feature"), """
                @smoke @login
                Scenario: Valid user logs in
                  Given user opens login page
                  When user logs in as "kailash"
                  Then dashboard is displayed
                """);

        Files.writeString(stepDirectory.resolve("LoginSteps.java"), """
                package com.acme.steps;

                import io.cucumber.java.en.Given;
                import io.cucumber.java.en.Then;
                import io.cucumber.java.en.When;

                public class LoginSteps {
                    @Given("user opens login page")
                    public void openLoginPage() {
                    }

                    @When("user logs in as {string}")
                    public void loginAsUser() {
                    }

                    @Then("^dashboard is displayed$")
                    public void dashboardIsDisplayed() {
                    }
                }
                """);

        Path output = tempDirectory.resolve("prepared");
        ImpactTrackerApp.main(new String[] {
                "prepare",
                "--repo-root", repo.toString(),
                "--output-dir", output.toString()
        });

        String scenarioIndex = Files.readString(output.resolve("scenario-index.csv"));
        String staticMap = Files.readString(output.resolve("static-scenario-step-map.tsv"));
        String unmatched = Files.readString(output.resolve("unmatched-steps.tsv"));

        assertTrue(scenarioIndex.contains("src/test/resources/features/login.feature"));
        assertTrue(staticMap.contains("com.acme.steps.LoginSteps\topenLoginPage"));
        assertTrue(staticMap.contains("com.acme.steps.LoginSteps\tloginAsUser"));
        assertTrue(staticMap.contains("com.acme.steps.LoginSteps\tdashboardIsDisplayed"));
        assertTrue(unmatched.lines().count() == 1);
    }
}

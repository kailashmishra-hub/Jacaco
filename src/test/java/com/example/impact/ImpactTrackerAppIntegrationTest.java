package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImpactTrackerAppIntegrationTest {
    @TempDir
    Path tempDirectory;

    @Test
    void buildsKnowledgeMapAndReportsImpactedScenarios() throws Exception {
        Path coverageDirectory = tempDirectory.resolve("coverage");
        Path outputDirectory = tempDirectory.resolve("map");
        Files.createDirectories(coverageDirectory);

        Path scenarioIndex = tempDirectory.resolve("scenario-index.csv");
        Files.writeString(scenarioIndex, """
                scenarioId,featurePath,line,name,tags
                login-valid-user,features/login.feature,8,Valid user logs in,@smoke @login
                """);

        Files.writeString(coverageDirectory.resolve("login-valid-user.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <report name="login-valid-user">
                  <package name="com/acme/steps">
                    <class name="com/acme/steps/LoginSteps" sourcefilename="LoginSteps.java">
                      <method name="userLogsIn" desc="()V" line="22">
                        <counter type="INSTRUCTION" missed="0" covered="21"/>
                      </method>
                    </class>
                  </package>
                </report>
                """);

        ImpactTrackerApp.main(new String[] {
                "build",
                "--scenario-index", scenarioIndex.toString(),
                "--coverage-dir", coverageDirectory.toString(),
                "--output-dir", outputDirectory.toString()
        });

        Path changedMethods = tempDirectory.resolve("changed-methods.txt");
        Files.writeString(changedMethods, "com.acme.steps.LoginSteps#userLogsIn()V\n");

        Path report = tempDirectory.resolve("impact-report.md");
        ImpactTrackerApp.main(new String[] {
                "impact",
                "--map", outputDirectory.resolve("scenario-method-map.tsv").toString(),
                "--changed-methods", changedMethods.toString(),
                "--output", report.toString()
        });

        assertTrue(Files.readString(outputDirectory.resolve("knowledge-map.json"))
                .contains("com.acme.steps.LoginSteps#userLogsIn()V"));
        assertEquals("""
                # Impacted Cucumber Scenarios

                ## com.acme.steps.LoginSteps#userLogsIn()V

                - features/login.feature:8 - Valid user logs in [@smoke @login]

                """, Files.readString(report));
    }
}

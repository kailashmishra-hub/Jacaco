package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MethodGraphCliIntegrationTest {
    @TempDir
    Path tempDirectory;

    @Test
    void buildsGraphAndComparesGraphSnapshots() throws Exception {
        Path baselineXml = writeJacocoXml("baseline.xml", "0", "10");
        Path currentXml = writeJacocoXml("current.xml", "2", "8");
        Path baselineOutput = tempDirectory.resolve("baseline");
        Path currentOutput = tempDirectory.resolve("current");

        ImpactTrackerApp.main(new String[] {
                "graph",
                "--jacoco-xml", baselineXml.toString(),
                "--output-dir", baselineOutput.toString()
        });
        ImpactTrackerApp.main(new String[] {
                "graph",
                "--jacoco-xml", currentXml.toString(),
                "--output-dir", currentOutput.toString()
        });

        Path changeReport = tempDirectory.resolve("changes.md");
        ImpactTrackerApp.main(new String[] {
                "compare-graph",
                "--baseline", baselineOutput.resolve("method-graph.tsv").toString(),
                "--current", currentOutput.resolve("method-graph.tsv").toString(),
                "--output", changeReport.toString()
        });

        assertTrue(Files.readString(baselineOutput.resolve("method-graph.dot"))
                .contains("com.acme.steps.LoginSteps"));
        assertTrue(Files.readString(baselineOutput.resolve("method-index.md"))
                .contains("com.acme.steps.LoginSteps#userLogsIn()V"));
        assertTrue(Files.readString(changeReport)
                .contains("COVERAGE_CHANGED: com.acme.steps.LoginSteps#userLogsIn()V"));
    }

    private Path writeJacocoXml(String fileName, String missed, String covered) throws Exception {
        Path report = tempDirectory.resolve(fileName);
        Files.writeString(report, """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
                <report name="Cucumber Impact Tracker">
                  <package name="com/acme/steps">
                    <class name="com/acme/steps/LoginSteps" sourcefilename="LoginSteps.java">
                      <method name="userLogsIn" desc="()V" line="22">
                        <counter type="INSTRUCTION" missed="%s" covered="%s"/>
                        <counter type="LINE" missed="0" covered="3"/>
                        <counter type="COMPLEXITY" missed="0" covered="1"/>
                        <counter type="METHOD" missed="0" covered="1"/>
                      </method>
                    </class>
                  </package>
                </report>
                """.formatted(missed, covered));
        return report;
    }
}

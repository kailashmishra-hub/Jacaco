package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JacocoMethodGraphReaderTest {
    @TempDir
    Path tempDirectory;

    @Test
    void readsMethodGraphNodesFromAggregateJacocoXml() throws Exception {
        Path report = tempDirectory.resolve("jacoco.xml");
        Files.writeString(report, """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
                <report name="Cucumber Impact Tracker">
                  <package name="com/acme/steps">
                    <class name="com/acme/steps/LoginSteps" sourcefilename="LoginSteps.java">
                      <method name="userLogsIn" desc="()V" line="22">
                        <counter type="INSTRUCTION" missed="0" covered="21"/>
                        <counter type="BRANCH" missed="1" covered="3"/>
                        <counter type="LINE" missed="0" covered="4"/>
                        <counter type="COMPLEXITY" missed="1" covered="2"/>
                        <counter type="METHOD" missed="0" covered="1"/>
                      </method>
                    </class>
                  </package>
                </report>
                """);

        List<MethodGraphNode> nodes = new JacocoMethodGraphReader().read(report);

        assertEquals(1, nodes.size());
        assertEquals("com.acme.steps.LoginSteps#userLogsIn()V", nodes.get(0).methodId());
        assertEquals("instruction=0/21;branch=1/3;line=0/4;complexity=1/2;method=0/1",
                nodes.get(0).coverageFingerprint());
    }
}

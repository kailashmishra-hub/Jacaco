package com.example.impact;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JacocoXmlCoverageReaderTest {
    @TempDir
    Path tempDirectory;

    @Test
    void readsOnlyMethodsWithCoveredInstructions() throws Exception {
        Path report = tempDirectory.resolve("scenario.xml");
        Files.writeString(report, """
                <?xml version="1.0" encoding="UTF-8"?>
                <report name="scenario">
                  <package name="com/acme/steps">
                    <class name="com/acme/steps/LoginSteps" sourcefilename="LoginSteps.java">
                      <method name="userLogsIn" desc="()V" line="17">
                        <counter type="INSTRUCTION" missed="0" covered="12"/>
                      </method>
                      <method name="unusedStep" desc="()V" line="25">
                        <counter type="INSTRUCTION" missed="8" covered="0"/>
                      </method>
                    </class>
                  </package>
                </report>
                """);

        Set<CoveredMethod> methods = new JacocoXmlCoverageReader().readCoveredMethods(report);

        assertEquals(Set.of(new CoveredMethod("com.acme.steps.LoginSteps", "userLogsIn", "()V", 17)), methods);
    }

    @Test
    void ignoresExternalJacocoDtd() throws Exception {
        Path report = tempDirectory.resolve("scenario-with-dtd.xml");
        Files.writeString(report, """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
                <report name="scenario">
                  <package name="com/acme/steps">
                    <class name="com/acme/steps/LoginSteps" sourcefilename="LoginSteps.java">
                      <method name="userLogsIn" desc="()V" line="17">
                        <counter type="INSTRUCTION" missed="0" covered="12"/>
                      </method>
                    </class>
                  </package>
                </report>
                """);

        Set<CoveredMethod> methods = new JacocoXmlCoverageReader().readCoveredMethods(report);

        assertEquals(Set.of(new CoveredMethod("com.acme.steps.LoginSteps", "userLogsIn", "()V", 17)), methods);
    }
}

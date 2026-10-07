package com.example.impact;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JacocoXmlCoverageReader {
    public Set<CoveredMethod> readCoveredMethods(Path jacocoXml) throws IOException {
        Set<CoveredMethod> methods = new LinkedHashSet<>();

        try (InputStream input = Files.newInputStream(jacocoXml)) {
            Element report = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(input)
                    .getDocumentElement();

            NodeList classNodes = report.getElementsByTagName("class");
            for (int index = 0; index < classNodes.getLength(); index++) {
                Element classElement = (Element) classNodes.item(index);
                String className = classElement.getAttribute("name").replace('/', '.');
                NodeList childNodes = classElement.getChildNodes();

                for (int child = 0; child < childNodes.getLength(); child++) {
                    Node node = childNodes.item(child);
                    if (node instanceof Element methodElement && "method".equals(methodElement.getTagName())) {
                        addIfCovered(methods, className, methodElement);
                    }
                }
            }
        } catch (SAXException exception) {
            throw new IOException("Invalid JaCoCo XML: " + jacocoXml, exception);
        } catch (Exception exception) {
            throw new IOException("Could not read JaCoCo XML: " + jacocoXml, exception);
        }

        return methods;
    }

    private void addIfCovered(Set<CoveredMethod> methods, String className, Element methodElement) {
        if (coveredInstructionCount(methodElement) == 0) {
            return;
        }

        methods.add(new CoveredMethod(
                className,
                methodElement.getAttribute("name"),
                methodElement.getAttribute("desc"),
                parseLine(methodElement.getAttribute("line"))));
    }

    private int coveredInstructionCount(Element methodElement) {
        NodeList counters = methodElement.getElementsByTagName("counter");
        for (int index = 0; index < counters.getLength(); index++) {
            Element counter = (Element) counters.item(index);
            if ("INSTRUCTION".equals(counter.getAttribute("type"))) {
                return Integer.parseInt(counter.getAttribute("covered"));
            }
        }
        return 0;
    }

    private int parseLine(String line) {
        if (line == null || line.isBlank()) {
            return -1;
        }
        return Integer.parseInt(line);
    }
}

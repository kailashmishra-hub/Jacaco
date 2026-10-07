package com.example.impact;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JacocoMethodGraphReader {
    public List<MethodGraphNode> read(Path jacocoXml) throws IOException {
        List<MethodGraphNode> nodes = new ArrayList<>();

        try (InputStream input = Files.newInputStream(jacocoXml)) {
            Element report = documentBuilderFactory()
                    .newDocumentBuilder()
                    .parse(input)
                    .getDocumentElement();

            NodeList packageNodes = report.getElementsByTagName("package");
            for (int packageIndex = 0; packageIndex < packageNodes.getLength(); packageIndex++) {
                Element packageElement = (Element) packageNodes.item(packageIndex);
                readPackage(nodes, packageElement);
            }
        } catch (SAXException exception) {
            throw new IOException("Invalid JaCoCo XML: " + jacocoXml, exception);
        } catch (Exception exception) {
            throw new IOException("Could not read JaCoCo XML: " + jacocoXml, exception);
        }

        return nodes;
    }

    private void readPackage(List<MethodGraphNode> nodes, Element packageElement) {
        String packageName = packageElement.getAttribute("name").replace('/', '.');
        NodeList childNodes = packageElement.getChildNodes();

        for (int child = 0; child < childNodes.getLength(); child++) {
            Node node = childNodes.item(child);
            if (node instanceof Element classElement && "class".equals(classElement.getTagName())) {
                readClass(nodes, packageName, classElement);
            }
        }
    }

    private void readClass(List<MethodGraphNode> nodes, String packageName, Element classElement) {
        String className = classElement.getAttribute("name").replace('/', '.');
        String sourceFile = classElement.getAttribute("sourcefilename");
        NodeList childNodes = classElement.getChildNodes();

        for (int child = 0; child < childNodes.getLength(); child++) {
            Node node = childNodes.item(child);
            if (node instanceof Element methodElement && "method".equals(methodElement.getTagName())) {
                Map<String, CoverageCounter> counters = counters(methodElement);
                nodes.add(new MethodGraphNode(
                        packageName,
                        className,
                        sourceFile,
                        methodElement.getAttribute("name"),
                        methodElement.getAttribute("desc"),
                        parseLine(methodElement.getAttribute("line")),
                        counters.getOrDefault("INSTRUCTION", new CoverageCounter(0, 0)),
                        counters.getOrDefault("BRANCH", new CoverageCounter(0, 0)),
                        counters.getOrDefault("LINE", new CoverageCounter(0, 0)),
                        counters.getOrDefault("COMPLEXITY", new CoverageCounter(0, 0)),
                        counters.getOrDefault("METHOD", new CoverageCounter(0, 0))));
            }
        }
    }

    private Map<String, CoverageCounter> counters(Element methodElement) {
        Map<String, CoverageCounter> counters = new LinkedHashMap<>();
        NodeList counterNodes = methodElement.getElementsByTagName("counter");
        for (int index = 0; index < counterNodes.getLength(); index++) {
            Element counter = (Element) counterNodes.item(index);
            counters.put(counter.getAttribute("type"), new CoverageCounter(
                    Integer.parseInt(counter.getAttribute("missed")),
                    Integer.parseInt(counter.getAttribute("covered"))));
        }
        return counters;
    }

    private int parseLine(String line) {
        if (line == null || line.isBlank()) {
            return -1;
        }
        return Integer.parseInt(line);
    }

    private DocumentBuilderFactory documentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (Exception ignored) {
            // Some XML parsers do not expose this feature; parsing still works without validation.
        }
        return factory;
    }
}

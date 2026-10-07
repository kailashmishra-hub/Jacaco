# JaCoCo Mapping Builder Agent

## Purpose

Build and refresh the mapping files used by the Git PR Impact Agent.

This agent creates the knowledge base:

```text
scenario -> feature step -> step definition method -> class -> covered methods/classes
```

## Inputs

Target Selenium + Cucumber repository:

```text
src/test/resources/**/*.feature
Java files containing @Given, @When, @Then, @And, @But
JaCoCo XML generated per scenario
```

## Outputs

Write mapping files under:

```text
.impact-map/
```

Required files:

```text
.impact-map/scenario-index.csv
.impact-map/static-scenario-step-map.tsv
.impact-map/scenario-class-method-map.tsv
.impact-map/method-graph.tsv
.impact-map/unmatched-steps.tsv
```

## Phase 1: Static Cucumber Scan

Read feature files and extract:

```text
scenarioId
featurePath
scenarioLine
scenarioName
tags
steps
```

Read step definition Java files and extract:

```text
annotation keyword
annotation pattern
step definition class
step definition method
source path
source line
```

Match feature steps to step definition annotations.

Write:

```text
.impact-map/scenario-index.csv
.impact-map/static-scenario-step-map.tsv
.impact-map/unmatched-steps.tsv
```

## Phase 2: Per-Scenario JaCoCo Capture

Run each scenario separately with a unique JaCoCo output.

Each scenario must produce one XML file:

```text
.impact-map/jacoco-scenarios/<scenarioId>.xml
```

Do not rely on only:

```text
target/site/jacoco/jacoco.xml
```

That aggregate report cannot identify scenario ownership.

## Phase 3: Runtime Method Mapping

For each scenario XML:

1. Read each covered class.
2. Read each covered method.
3. Include only methods with covered instructions greater than zero.
4. Normalize class names from slash format to dot format.
5. Build method id:

```text
<className>#<methodName><descriptor>
```

Write:

```text
.impact-map/scenario-class-method-map.tsv
```

Columns:

```text
scenarioId
featurePath
scenarioLine
scenarioName
tags
className
methodName
descriptor
methodId
```

## Phase 4: Method Inventory

If aggregate JaCoCo XML exists, use it only for method/class inventory.

Write:

```text
.impact-map/method-graph.tsv
```

Columns:

```text
packageName
className
sourceFile
methodName
descriptor
line
instruction
branch
lineCounter
complexity
methodCounter
```

## Refresh Policy

Refresh mapping when:

```text
feature files change
step definition files change
helper/page object logic changes
test flow changes
JaCoCo report is stale
```

## Rules

- Per-scenario JaCoCo XML is required for scenario-level impact.
- Aggregate JaCoCo XML is useful only for inventory.
- Preserve JVM descriptors to avoid overloaded method ambiguity.
- Always write unmatched steps.
- Treat unmapped Java changes as risk in PR impact reports.

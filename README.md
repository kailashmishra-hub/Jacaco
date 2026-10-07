# Cucumber Impact Tracker

This project is a starting point for a JaCoCo-backed impact tracker for Cucumber test frameworks.

The goal is to build a knowledge map that answers:

```text
If this step definition or helper method changed, which feature scenarios should run?
```

## How the Mapping Works

1. Run each Cucumber scenario with a separate JaCoCo output.
2. Convert that scenario's JaCoCo execution data into XML.
3. Create a scenario index that tells the tracker which XML file belongs to which feature scenario.
4. Build a knowledge map from covered Java methods to Cucumber scenarios.
5. Compare changed method IDs against that map to produce impacted scenarios.

This repository currently implements steps 3, 4, and 5. The next layer is a framework adapter that launches your real Cucumber tests scenario-by-scenario and emits the per-scenario JaCoCo XML files.

## Requirements

- Java 17 or newer
- Maven 3.8 or newer

## Build and Test

```powershell
mvn verify
```

This runs unit tests, generates the normal JaCoCo coverage report, and enforces the 80% line coverage rule for this tool itself.

## Try the Example

Build the JAR:

```powershell
mvn package
```

Build a knowledge map from the sample scenario index and per-scenario JaCoCo XML files:

```powershell
java -jar target/cucumber-impact-tracker-1.0.0-SNAPSHOT.jar build --scenario-index examples/scenario-index.csv --coverage-dir examples/jacoco --output-dir target/impact-map
```

Analyze changed methods:

```powershell
java -jar target/cucumber-impact-tracker-1.0.0-SNAPSHOT.jar impact --map target/impact-map/scenario-method-map.tsv --changed-methods examples/changed-methods.txt --output target/impact-report.md
```

Open the generated report:

```text
target/impact-report.md
```

## Build a Method Knowledge Graph from JaCoCo XML

If you only have a normal aggregate JaCoCo XML report, like:

```text
target/site/jacoco/jacoco.xml
```

you can still create a code-side knowledge graph:

```powershell
java -jar target/cucumber-impact-tracker-1.0.0-SNAPSHOT.jar graph --jacoco-xml target/site/jacoco/jacoco.xml --output-dir target/method-graph
```

This writes:

```text
target/method-graph/method-graph.tsv
target/method-graph/method-graph.dot
target/method-graph/method-index.md
```

To detect method graph changes between two runs:

```powershell
java -jar target/cucumber-impact-tracker-1.0.0-SNAPSHOT.jar compare-graph --baseline target/baseline-method-graph.tsv --current target/method-graph/method-graph.tsv --output target/method-graph-changes.md
```

The compare command reports added methods, removed methods, and methods whose JaCoCo coverage counters changed. A JaCoCo XML file does not contain method body hashes, so exact source-code change detection still needs a Git/source parser adapter.

## Scenario Index Format

```csv
scenarioId,featurePath,line,name,tags
login-valid-user,features/login.feature,8,Valid user logs in,@smoke @login
```

Each `scenarioId` must have a matching JaCoCo XML report:

```text
examples/jacoco/login-valid-user.xml
```

## Changed Method Format

Changed methods are represented with JVM descriptors so overloaded methods stay precise:

```text
com.acme.steps.LoginSteps#userLogsIn()V
com.acme.helpers.AuthHelper#validateToken(Ljava/lang/String;)Z
```

## Intended Real-Framework Architecture

```text
Cucumber scenario execution
        |
        v
Per-scenario JaCoCo XML files
        |
        v
Scenario index + XML coverage reader
        |
        v
Knowledge map: Java method -> Cucumber scenarios
        |
        v
Git diff / changed methods
        |
        v
Impacted feature scenarios
```

## What Still Needs Framework-Specific Adapters

- Detect Cucumber scenarios from your actual feature files.
- Run each scenario independently with a unique JaCoCo destination file.
- Convert each `.exec` file to JaCoCo XML.
- Detect changed step/helper methods from a Git diff or pull request.
- Optionally include static call-chain expansion so a changed helper maps through step definitions even if direct scenario coverage is missing.

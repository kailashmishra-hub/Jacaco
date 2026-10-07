# JaCoCo Mapping Instructions

Use this file as the agent instruction source for building a relationship map in a Selenium + Cucumber Java repository.

## Goal

Build this relationship:

```text
scenario -> feature step -> step definition method -> class -> covered helper methods/classes
```

The final mapping should help answer:

```text
If this Java method or class changes, which Cucumber scenarios are impacted?
```

## Important JaCoCo Limitation

An aggregate JaCoCo report such as:

```text
target/site/jacoco/jacoco.xml
```

can identify:

```text
className -> methodName -> descriptor -> source line -> coverage counters
```

It cannot identify which Cucumber scenario executed a method.

To map methods back to scenarios, collect one JaCoCo XML report per scenario.

## Required Inputs

The agent should gather:

```text
1. Feature files
   src/test/resources/**/*.feature

2. Step definition files
   Java files containing @Given, @When, @Then, @And, @But

3. JaCoCo XML files
   One XML file per executed scenario
```

## Step 1: Build Scenario Index

Parse every `.feature` file and extract:

```text
scenarioId
featurePath
scenarioLine
scenarioName
tags
steps
```

Recommended scenario id:

```text
<feature-path>-<scenario-line>-<scenario-name-slug>
```

Example:

```text
src-test-resources-features-login-login-logout-feature-3-login-into-the-application
```

Write:

```text
scenario-index.csv
```

Columns:

```text
scenarioId,featurePath,line,name,tags
```

## Step 2: Build Static Step Definition Map

Parse Java step definition annotations:

```java
@Given("I am on the Login page URL {string}")
@When("^I enter username as \"([^\"]*)\"$")
@Then("I am logged in")
```

For each annotation, capture:

```text
step keyword
step pattern
step definition class
step definition method
source file
source line
```

Then match feature steps to annotation patterns.

Write:

```text
static-scenario-step-map.tsv
```

Columns:

```text
scenarioId
featurePath
scenarioLine
scenarioName
stepLine
stepKeyword
stepText
stepDefinitionClass
stepDefinitionMethod
stepDefinitionSource
stepDefinitionLine
pattern
```

This file gives:

```text
scenario -> feature step -> step definition class/method
```

## Step 3: Run JaCoCo Per Scenario

Run each scenario separately with a unique JaCoCo destination file.

Conceptually:

```text
scenario A -> target/jacoco-scenarios/scenario-a.exec
scenario B -> target/jacoco-scenarios/scenario-b.exec
```

Then convert each `.exec` file to XML:

```text
target/jacoco-scenarios/scenario-a.xml
target/jacoco-scenarios/scenario-b.xml
```

The XML filename should match `scenarioId`.

Example:

```text
target/jacoco-scenarios/src-test-resources-features-login-login-logout-feature-3-login-into-the-application.xml
```

## Step 4: Read JaCoCo XML Method Coverage

For each scenario XML, read:

```xml
<class name="com/example/LoginSteps" sourcefilename="LoginSteps.java">
  <method name="login" desc="()V" line="42">
    <counter type="INSTRUCTION" missed="0" covered="15"/>
  </method>
</class>
```

Only include methods where covered instruction count is greater than zero.

Normalize class names:

```text
com/example/LoginSteps -> com.example.LoginSteps
```

Create method id:

```text
<className>#<methodName><descriptor>
```

Example:

```text
com.example.LoginSteps#login()V
```

## Step 5: Write Final Mapping

Write:

```text
scenario-class-method-map.tsv
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

This file gives:

```text
scenario -> class -> method
```

When joined with `static-scenario-step-map.tsv`, it gives:

```text
scenario -> feature step -> step definition -> covered classes/methods
```

## Step 6: Impact Lookup

When a Java method changes:

```text
com.example.LoginSteps#login()V
```

Search `scenario-class-method-map.tsv` for that method id.

Return all matching scenarios:

```text
featurePath
scenarioLine
scenarioName
tags
```

If a helper method changes, the same lookup works if that helper was covered during the scenario run.

## Expected Output Files

```text
scenario-index.csv
static-scenario-step-map.tsv
scenario-class-method-map.tsv
unmatched-steps.tsv
impact-report.md
```

## Agent Rules

- Do not rely on aggregate `jacoco.xml` for scenario mapping.
- Use aggregate `jacoco.xml` only for method/class inventory.
- Use per-scenario JaCoCo XML for scenario impact mapping.
- Keep JVM descriptors in method ids to avoid ambiguity with overloaded methods.
- Always report unmatched feature steps separately.
- Do not assume a changed class impacts every scenario unless no method-level mapping exists.

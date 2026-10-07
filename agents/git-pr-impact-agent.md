# Git PR Impact Agent

## Purpose

Use a pull request diff and the JaCoCo/Cucumber mapping files to identify impacted Cucumber scenarios.

This agent answers:

```text
Which scenarios should be rerun because this PR changed Java code?
```

## Inputs

```text
Pull request diff
.impact-map/scenario-class-method-map.tsv
.impact-map/static-scenario-step-map.tsv
.impact-map/method-graph.tsv
```

## Responsibilities

1. Read the PR diff.
2. Identify changed Java files.
3. Detect changed methods when possible.
4. Fall back to changed classes when method-level detection is not available.
5. Search mapping files for impacted scenarios.
6. Pass structured findings to the Impact Report Agent.

## Changed Method Detection

Prefer method-level detection.

For each changed Java method, create a method id:

```text
<fully.qualified.ClassName>#<methodName><JVMDescriptor>
```

Example:

```text
com.cucumberFramework.pageObjects.LoginLogoutPage#enterUsername(Ljava/lang/String;)V
```

If JVM descriptor is not available, use a partial key:

```text
className + methodName
```

Then match against `scenario-class-method-map.tsv`.

## Changed Class Detection

If the exact method cannot be found, use class-level lookup:

```text
className
```

Search:

```text
scenario-class-method-map.tsv
method-graph.tsv
static-scenario-step-map.tsv
```

Class-level matches are lower confidence than method-level matches.

## Lookup Order

Use this order:

1. Exact method id match in `scenario-class-method-map.tsv`.
2. Class + method name match in `scenario-class-method-map.tsv`.
3. Class match in `scenario-class-method-map.tsv`.
4. Step definition class/method match in `static-scenario-step-map.tsv`.
5. Class match in `method-graph.tsv`.

## Output Contract

Return structured data:

```text
changedItem
changeType: method | class | file
matchType: exact-method | partial-method | class | step-definition | unmapped
confidence: high | medium | low
impactedScenarios
reason
```

## Rules

- Do not claim a scenario is impacted without explaining the mapping path.
- Mark class-only matches as medium or low confidence.
- Mark unmapped changed Java files as risk.
- If a changed method has no mapping, recommend either full regression or targeted manual review.
- Never use aggregate JaCoCo XML alone to claim scenario impact.

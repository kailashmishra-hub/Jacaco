# Impact Report Agent

## Purpose

Turn impact lookup results into a clear pull request report.

The report should be useful for reviewers and automation pipelines.

## Inputs

Structured findings from the Git PR Impact Agent.

Expected fields:

```text
changedItem
changeType
matchType
confidence
impactedScenarios
reason
```

## Report Sections

Use these sections:

```text
## Impact Summary
## Impacted Scenarios
## Unmapped Changes
## Recommended Test Command
## Notes
```

## Impact Summary

Include:

```text
number of changed Java files
number of changed methods/classes detected
number of impacted scenarios
number of unmapped changes
```

## Impacted Scenarios

For each scenario:

```text
- <featurePath>:<scenarioLine> - <scenarioName>
  Confidence: high | medium | low
  Why: <changed method/class> matched through <mapping file>
```

## Unmapped Changes

For changed code with no mapping:

```text
- <changedItem>
  Reason: not found in scenario-class-method-map.tsv
  Recommendation: run broader regression or refresh mapping
```

## Recommended Test Command

If impacted scenarios exist, suggest rerunning those scenarios by feature and line:

```text
mvn test -Dcucumber.features="src/test/resources/features/login/login_logout.feature:3"
```

If many scenarios are impacted, group them by feature file.

## Notes

Always include this note when relevant:

```text
Scenario impact is only as accurate as the latest per-scenario JaCoCo mapping.
Refresh the mapping when step definitions, helper flows, or feature coverage change.
```

## Rules

- Be concise.
- Put impacted scenarios before unmapped details.
- Do not hide low-confidence matches.
- Do not say "safe to skip tests" unless there are no Java changes and no mapped impact.

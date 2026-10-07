# Git Impact Pull Request Agent Blueprint

This folder defines the agent approach for using JaCoCo with a Git pull request impact workflow.

The goal is not to create another large Java framework. The goal is to give an AI/code agent clear operating instructions and file contracts.

## Agent Flow

```text
JaCoCo Mapping Builder Agent
        |
        v
mapping files
        |
        v
Git PR Impact Agent
        |
        v
Impact Report Agent
        |
        v
PR comment / notification
```

## Agents

1. `jacoco-mapping-builder-agent.md`
   - Builds the long-lived knowledge map.
   - Produces scenario, step definition, class, and method mapping files.

2. `git-pr-impact-agent.md`
   - Reads a pull request diff.
   - Detects changed Java files, classes, and methods.
   - Looks up those changes in the mapping files.

3. `impact-report-agent.md`
   - Converts lookup results into a readable PR impact report.
   - Explains impacted scenarios and unmapped risky changes.

## Shared Mapping Files

The agents should exchange these files:

```text
.impact-map/scenario-index.csv
.impact-map/static-scenario-step-map.tsv
.impact-map/scenario-class-method-map.tsv
.impact-map/method-graph.tsv
.impact-map/unmatched-steps.tsv
```

## Recommended PR Output

```text
Impacted Scenarios
- features/login/login_logout.feature:3 - Login into the application with valid credentials
  Changed method: com.app.pages.LoginPage#enterUsername(Ljava/lang/String;)V
  Covered by: login scenario runtime mapping

Unmapped Changes
- com.app.helpers.NewHelper#newMethod()V
  Reason: method not found in scenario-class-method-map.tsv
```

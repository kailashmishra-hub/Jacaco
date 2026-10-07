# JaCoCo Demo

This is a small Java Maven project configured with JUnit 5 and JaCoCo.

## Requirements

- Java 17 or newer
- Maven 3.8 or newer

## Run Tests and Generate Coverage

```powershell
mvn test
```

The HTML coverage report is generated at:

```text
target/site/jacoco/index.html
```

## Enforce Coverage

```powershell
mvn verify
```

The build fails if total line coverage drops below 80%.

## Project Layout

```text
src/main/java/com/example/jacoco/Calculator.java
src/test/java/com/example/jacoco/CalculatorTest.java
pom.xml
```

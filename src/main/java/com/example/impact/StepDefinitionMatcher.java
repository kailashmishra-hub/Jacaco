package com.example.impact;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StepDefinitionMatcher {
    public StepDefinitionDescriptor firstMatch(ScenarioStep step, List<StepDefinitionDescriptor> definitions) {
        for (StepDefinitionDescriptor definition : definitions) {
            if (matches(step.text(), definition.pattern())) {
                return definition;
            }
        }
        return null;
    }

    private boolean matches(String stepText, String pattern) {
        String regex = looksLikeRegex(pattern) ? pattern : cucumberExpressionToRegex(pattern);
        try {
            return Pattern.compile(regex).matcher(stepText).matches();
        } catch (PatternSyntaxException exception) {
            return stepText.equals(pattern);
        }
    }

    private boolean looksLikeRegex(String pattern) {
        return pattern.startsWith("^") || pattern.endsWith("$") || pattern.contains(".*");
    }

    private String cucumberExpressionToRegex(String expression) {
        StringBuilder regex = new StringBuilder("^");
        for (int index = 0; index < expression.length(); index++) {
            char current = expression.charAt(index);
            if (current == '{') {
                int end = expression.indexOf('}', index);
                if (end > index) {
                    regex.append(parameterRegex(expression.substring(index + 1, end)));
                    index = end;
                    continue;
                }
            }

            if ("\\.^$|?*+()[{".indexOf(current) >= 0) {
                regex.append('\\');
            }
            regex.append(current);
        }
        return regex.append('$').toString();
    }

    private String parameterRegex(String type) {
        return switch (type) {
            case "int", "long", "short", "byte" -> "-?\\d+";
            case "float", "double", "bigdecimal" -> "-?\\d+(?:\\.\\d+)?";
            case "word" -> "\\w+";
            case "string" -> "\"[^\"]*\"|'[^']*'";
            default -> ".+";
        };
    }
}

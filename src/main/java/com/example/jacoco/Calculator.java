package com.example.jacoco;

public class Calculator {
    public int add(int left, int right) {
        return left + right;
    }

    public int subtract(int left, int right) {
        return left - right;
    }

    public int multiply(int left, int right) {
        return left * right;
    }

    public int divide(int left, int right) {
        if (right == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }

        return left / right;
    }
}

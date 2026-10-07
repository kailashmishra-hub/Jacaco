package com.example.jacoco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CalculatorTest {
    private final Calculator calculator = new Calculator();

    @Test
    void addsTwoNumbers() {
        assertEquals(7, calculator.add(3, 4));
    }

    @Test
    void subtractsTwoNumbers() {
        assertEquals(2, calculator.subtract(7, 5));
    }

    @Test
    void multipliesTwoNumbers() {
        assertEquals(42, calculator.multiply(6, 7));
    }

    @Test
    void dividesTwoNumbers() {
        assertEquals(4, calculator.divide(20, 5));
    }

    @Test
    void rejectsDivisionByZero() {
        assertThrows(IllegalArgumentException.class, () -> calculator.divide(10, 0));
    }
}

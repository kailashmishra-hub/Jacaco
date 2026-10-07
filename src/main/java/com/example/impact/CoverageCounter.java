package com.example.impact;

public record CoverageCounter(int missed, int covered) {
    public int total() {
        return missed + covered;
    }

    public String compact() {
        return missed + "/" + covered;
    }

    public static CoverageCounter parse(String value) {
        String[] parts = value.split("/", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Counter must be missed/covered: " + value);
        }
        return new CoverageCounter(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}

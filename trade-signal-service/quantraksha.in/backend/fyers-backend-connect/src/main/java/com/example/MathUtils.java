package com.example;

public class MathUtils {

    public static double mround(double number, double multiple) {
        if (multiple == 0) return 0;
        double result = Math.round(number / multiple) * multiple;
        return Math.round(result * 100.0) / 100.0;
    }
}

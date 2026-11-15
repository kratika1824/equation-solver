package com.example.equationsolver.util;

import java.math.BigDecimal;

/** Small helpers so 11.0 is shown as 11 but 2.5 stays 2.5. */
public final class NumberFormatter {

    private NumberFormatter() {
    }

    public static String format(double value) {
        if (isWhole(value)) {
            return Long.toString((long) value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /** Whole numbers become Long so JSON shows 11 instead of 11.0. */
    public static Number toJsonNumber(double value) {
        if (isWhole(value)) {
            return (long) value;
        }
        return value;
    }

    private static boolean isWhole(double value) {
        return value == Math.rint(value) && Math.abs(value) < 1e15;
    }
}

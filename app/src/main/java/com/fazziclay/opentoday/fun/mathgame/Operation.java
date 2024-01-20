package com.fazziclay.opentoday.fun.mathgame;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.util.RandomUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Operation {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    UNKNOWN("?");

    private final String s;

    Operation(String s) {
        this.s = s;
    }

    public int apply(int i1, int i2) {
        if (i2 == 0 && this == DIVIDE) return 0;
        return switch (this) {
            case ADD -> i1 + i2;
            case SUBTRACT -> i1 - i2;
            case MULTIPLY -> i1 * i2;
            case DIVIDE -> i1 / i2;
            case UNKNOWN -> i1;
        };
    }

    @NonNull
    @Override
    public String toString() {
        return s;
    }

    public static Operation random() {
        return values()[RandomUtil.nextInt(values().length)];
    }

    public static Operation fromString(String s) {
        for (Operation value : values()) {
            if (value.s.equals(s)) return value;
        }
        return null;
    }

    public static Set<Operation> parse(Set<String> strings) {
        Set<Operation> operations = new HashSet<>();
        for (String string : strings) {
            operations.add(fromString(string));
        }
        return operations;
    }

    public String forNumbers(int val1, int val2) {
        return switch (this) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE -> String.format("%s %s %s", val1, this.s, val2);
            case UNKNOWN -> null;
        };
    }
}

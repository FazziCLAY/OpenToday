package com.betterbrainmemory.opentoday.util;

public class Checks {
    public static void throwIsNull(Object nn, Object errorMessage) {
        if (nn == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
    }

    public static void throwIsNotNull(Object n, Object errorMessage) {
        if (n != null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
    }
}

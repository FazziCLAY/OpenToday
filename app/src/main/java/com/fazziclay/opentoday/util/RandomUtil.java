package com.fazziclay.opentoday.util;

import java.util.Random;

public class RandomUtil {
    private static Random RANDOM = null;

    public static Random getRandom() {
        if (RANDOM == null) {
            RANDOM = new Random();
        }
        return RANDOM;
    }

    public static int nextInt() {
        return getRandom().nextInt();
    }

    public static int nextIntPositive() {
        return Math.abs(nextInt());
    }

    public static int nextInt(int bound) {
        return getRandom().nextInt(bound);
    }

    public static boolean nextBoolean() {
        return getRandom().nextBoolean();
    }
}

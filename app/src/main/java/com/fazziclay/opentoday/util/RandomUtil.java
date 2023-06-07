package com.fazziclay.opentoday.util;

import com.fazziclay.opentoday.app.OptionalField;

import java.util.Random;

public class RandomUtil {
    private static final OptionalField<Random> RANDOM = new OptionalField<>(Random::new);

    public static void free() {
        RANDOM.free();
    }

    public static Random getRandom() {
        return RANDOM.get();
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

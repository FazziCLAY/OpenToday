package com.betterbrainmemory.opentoday.util;

import com.betterbrainmemory.opentoday.app.OptionalField;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Utility class for quickly get unsafe random functions
 */
public final class RandomUtil {
    private static final OptionalField<Random> RANDOM = new OptionalField<>(Random::new);

    private RandomUtil() {}

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

    public static int bounds(int min, int max) {
        if (min > max) {
            int tempMin = min;
            min = max;
            max = tempMin;
        }
        return getRandom().nextInt(max + 1 - min) + min;
    }

    @Nullable
    public static <T> T randomOfList(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(bounds(0, list.size()));
    }
}

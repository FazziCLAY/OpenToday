package com.fazziclay.opentoday.app.items.item;

import com.fazziclay.opentoday.app.App;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Transform {
    /**
     * @param from from
     * @param to to
     * @return {@link Result}
     */
    public static Result transform(Item from, ItemsRegistry.ItemInfo to) {
        if (from == null || to == null) {
            return Result.NOT_ALLOW;
        }

        // not allow "copy" with transform
        if (from.getClass() == to.getClassType()) {
            return Result.NOT_ALLOW;
        }

        if (!to.isCompatibility(App.get().getFeatureFlags())) {
            return Result.NOT_ALLOW;
        }

        final ItemFactory<?> factory = to.getFactory();
        return factory.transform(from);
    }

    public static boolean isAllow(Item item, ItemsRegistry.ItemInfo type) {
        return transform(item, type).isAllow();
    }


    public static class Result {
        public static Result NOT_ALLOW = new Result(false, null);

        public static Result allow(@NotNull Supplier<Item> transformation) {
            if (transformation == null) {
                throw new IllegalArgumentException("transformation can't be null for allowed transformation!");
            }
            return new Result(true, transformation);
        }

        private final boolean allow;
        private final Supplier<Item> result;

        private Result(boolean allow, Supplier<Item> result) {
            this.allow = allow;
            this.result = result;
        }

        public boolean isAllow() {
            return allow;
        }

        // create transformed item
        public Item generate() {
            return result.get();
        }
    }
}

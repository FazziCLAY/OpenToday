package com.fazziclay.opentoday.app;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public class OptionalField <T> {
    private final InitSupplier<T> supplier;
    private final FreeRunnable<T> freeRunnable;
    private T value = null;

    public OptionalField(final InitSupplier<T> supplier) {
        this.supplier = supplier;
        this.freeRunnable = null;
    }

    public OptionalField(final InitSupplier<T> supplier, final FreeRunnable<T> freeRunnable) {
        this.supplier = supplier;
        this.freeRunnable = freeRunnable;
    }

    @NonNull
    @NotNull
    public T get() {
        if (value == null) {
            value = supplier.supplier();
        }
        return value;
    }

    public boolean isSet() {
        return value != null;
    }

    public void free() {
        if (isSet()) {
            if (freeRunnable != null) freeRunnable.preFree(value);
        }
        value = null;
    }

    public interface InitSupplier <T> {
        T supplier();
    }

    public interface FreeRunnable<T> {
        void preFree(T f);
    }
}

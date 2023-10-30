package com.fazziclay.opentoday.app;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.util.profiler.Profiler;

import org.jetbrains.annotations.NotNull;

public class OptionalField <T> {
    private static final Profiler PROFILER = App.createProfiler("OptionalField");

    private final InitSupplier<T> supplier;
    private final FreeRunnable<T> freeRunnable;
    private final Validator<T> validator;
    private T value = null;

    public OptionalField(final InitSupplier<T> supplier) {
        this.supplier = supplier;
        this.freeRunnable = null;
        this.validator = null;
    }

    public OptionalField(final InitSupplier<T> supplier, final Validator<T> validator) {
        this.supplier = supplier;
        this.freeRunnable = null;
        this.validator = validator;
    }

    public OptionalField(final InitSupplier<T> supplier, final FreeRunnable<T> freeRunnable) {
        this.supplier = supplier;
        this.freeRunnable = freeRunnable;
        this.validator = null;
    }


    public OptionalField(final InitSupplier<T> supplier, final FreeRunnable<T> freeRunnable, final Validator<T> validator) {
        this.supplier = supplier;
        this.freeRunnable = freeRunnable;
        this.validator = validator;
    }

    @NonNull
    @NotNull
    public T get() {
        PROFILER.push("get");
        if (value == null) {
            PROFILER.push("supplier");
            value = supplier.supplier();
            PROFILER.pop();
        }
        if (validator != null) {
            PROFILER.push("validate");
            value = validator.validate(value);
            PROFILER.pop();
        }
        PROFILER.pop();
        return value;
    }

    public boolean isSet() {
        return value != null;
    }

    public void free() {
        PROFILER.push("free");
        if (isSet()) {
            if (freeRunnable != null) freeRunnable.preFree(value);
        }
        value = null;
        PROFILER.pop();
    }

    public interface InitSupplier<T> {
        T supplier();
    }

    public interface FreeRunnable<T> {
        void preFree(T f);
    }

    public interface Validator<T> {
        T validate(T current);
    }
}

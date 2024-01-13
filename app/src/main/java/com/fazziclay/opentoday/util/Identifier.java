package com.fazziclay.opentoday.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public final class Identifier {
    @NotNull
    private final String namespace;

    @NotNull
    private final String name;


    private Identifier(@NonNull String namespace, @NonNull String name) {
        if (namespace.isBlank() || namespace.isEmpty() || name.isBlank() || namespace.isEmpty()) {
            throw new RuntimeException("Identifier can't be empty values!");
        }
        this.namespace = namespace;
        this.name = name;
    }

    @NonNull
    public static Identifier of(String s) {
        if (!s.contains(":")) throw new RuntimeException("Identifier.of argument required contain ':' separator");
        final String[] split = s.split(":");
        return new Identifier(split[0], split[1]);
    }

    @NonNull
    public String string() {
        return toString();
    }

    @NonNull
    @Override
    public String toString() {
        return namespace + ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        if (!namespace.equals(that.namespace)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
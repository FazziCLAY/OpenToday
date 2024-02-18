package com.betterbrainmemory.opentoday.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public final class Identifier {
    @NotNull
    private final String namespace;

    @NotNull
    private final String path;


    private Identifier(@NonNull String namespace, @NonNull String path) {
        if (namespace.isBlank() || namespace.isEmpty() || path.isBlank() || namespace.isEmpty()) {
            throw new RuntimeException("Identifier can't be empty values!");
        }
        this.namespace = namespace;
        this.path = path;
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
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        if (!namespace.equals(that.namespace)) return false;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}

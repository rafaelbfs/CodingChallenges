package io.github.rafaelbfs.sparsearrays.util;

import io.github.rafaelbfs.sparsearrays.RangeTree;

public record Pair<T, U>(T fst, U snd) {
    protected static Pair<?, ?> EMPTY = new Pair<>(null, null);

    public static <T, U> Pair<T, U> of(T fst, U snd) {
        return new Pair<>(fst, snd);
    }

    public static <T, U> Pair<T, U> empty() {
        return (Pair<T, U>) EMPTY;
    }

    public boolean isEmpty() {
        return fst == null && snd == null;
    }

    public boolean hasLeft() {
        return fst != null;
    }

    public boolean hasRight() {
        return snd != null;
    }

    public boolean hasBoth() {
        return hasLeft() && hasRight();
    }

    public T left() {
        return fst;
    }

    public U right() {
        return snd;
    }
}

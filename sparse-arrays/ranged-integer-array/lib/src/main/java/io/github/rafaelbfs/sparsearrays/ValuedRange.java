package io.github.rafaelbfs.sparsearrays;

import java.util.function.BinaryOperator;

public class ValuedRange {
    protected int start;
    protected int end;
    protected long value;

    public ValuedRange(int start, int end, long value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public long update(BinaryOperator<Long> op, long other) {
        value = op.apply(value, other);
        return value;
    }

    public boolean below(ValuedRange other) {
        return this.end < other.start;
    }

    public boolean above(ValuedRange other) {
        return this.start > other.end;
    }

    public String toString() {
        return "[%d| %d |%d]".formatted(this.start, this.value, this.end);
    }

    public boolean contains(ValuedRange other) {
        return this.start <= other.start && this.end >= other.end;
    }

    public boolean same(ValuedRange other) {
        return this.start == other.start && this.end == other.end;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ValuedRange that)) return false;

        return start == that.start && end == that.end && value == that.value;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + Long.hashCode(value);
        return result;
    }
}

package io.github.rafaelbfs.sparsearrays;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.LongBinaryOperator;

/**
 * NO OVERLAPPING RANGES
 */
public class RangeSparseArray {
    private static final Comparator<Range> BY_START = Comparator.comparingInt(o -> o.start);
    protected final SortedSet<Range> ranges = new TreeSet<>(BY_START);

    public final int length;

    public RangeSparseArray(int length) {
        this.length = length;
    }

    public static class Range implements Comparable<Range> {
        protected int start;
        protected int end;
        protected long value;


        public Range(final int start, final int end, final long value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public Range(final int start) {
            this(start, Integer.MAX_VALUE, 0);
        }

        public boolean contains(Range other) {
            return this.start <= other.start && this.end >= other.end;
        }

        public boolean same(Range other) {
            return this.start == other.start && this.end == other.end;
        }

        public String toString() {
            return "[%d <-> %d] => %d".formatted(this.start, this.end, this.value);
        }

        @Override
        /**
         * This method needs to be specialized for insertions, because when the query is executed, the ranges
         * in the set won't overlap
         */
        public int compareTo(Range o) {
            if (this.start == o.start && this.end == o.end) return Long.compare(value, o.value);

            if (this.end < o.start) return -1;

            if (this.start > o.end) return 1;

            return Integer.compare(this.start, o.start);
        }
    }



    public void addValue(Range query) {
        if (query.value == 0L) {
            return;
        }
        // the new range does not overlap with existing ones
        /// / CASE 1
        if (ranges.isEmpty() || query.end < ranges.first().start || query.start > ranges.last().end) {
            ranges.add(query);
            return;
        }

        var q = new Range(query.start, query.end, query.value);
        var newRanges = new TreeSet<Range>(BY_START);
        /// ////// q starts before rng.start but ends before rng.end
        for (Range rng : ranges.subSet(new Range(0, q.start, q.value),
                new Range(q.end, length, q.value))) {
            if (q.same(rng)) {
                rng.value += query.value;
                q = null;
                break;
            }

            if (q.start > rng.end) {
                continue;
            }

            if (rng.contains(q)) {
                // head
                if (q.start > rng.start) {
                    newRanges.add(new Range(rng.start, q.start - 1, rng.value));
                }
                // tail
                if (q.end < rng.end) {
                    newRanges.add(new Range(q.end + 1, rng.end, rng.value));
                }
                rng.value += q.value;
                rng.start = q.start;
                rng.end = q.end;
                q = null;
                break; // query is entirely processed
            }

            if (q.contains(rng)) {
                if (rng.start > q.start) {
                    newRanges.add(new Range(q.start, q.start - 1, q.value));
                    // LLM-generated (!!WRONG!!) newRanges.add(new Range(rng.start, q.start - 1, rng.value)); //
                }
                rng.start = q.start;
                rng.value += q.value;
                q.start = rng.end + 1;

                continue; // there may be a range remaining in the query
            }

            /// ////// q starts before rng.start but ends before rng.end

            // case when query ends before rng.start
            if (q.start < rng.start) {
                if (q.end < rng.start) {
                    break; // q will be added as is now
                }
                newRanges.add(new Range(q.start, rng.start - 1, q.value));
                q.start = rng.start;
                if (q.end < rng.end) {
                    q.value += rng.value;
                    rng.start = q.end + 1;
                    break; // if q is not null it is added after for-loop
                } // if q ends after rng.end, it contains rng
            } else {
                assert q.end > rng.end; // q must end after rng.end here, otherwise it is contained in rng, which was handled already
                if (rng.start < q.start) {
                    newRanges.add(new Range(rng.start, q.start - 1, rng.value));
                    rng.start = q.start;
                }
                rng.value += q.value;
                q.start = rng.end + 1;
            }
        }
        if (q != null) {
            ranges.add(q);
        }
        ranges.addAll(newRanges);
    }
}

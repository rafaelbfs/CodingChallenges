package io.github.rafaelbfs.sparsearrays;

import io.github.rafaelbfs.sparsearrays.util.Pair;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RangeAvlTree {
    private static final int CELL_WIDTH = 36;
    private static final String CELL_FORMAT = "%" + CELL_WIDTH + "s";

    private static final NilNode NIL = new NilNode();
    protected enum Direction {LEFT, RIGHT};
    protected enum Case {LL, LR, RR, RL, NONE};

    protected static class Node {
        protected ValuedRange range;
        protected int height;
        protected Node left;
        protected Node right;

        public Node(ValuedRange range) {
            this.range = range;
            this.left = NIL;
            this.right = NIL;
            this.height = 1;
        }

        protected Node() {
            this(null);
        }

        public boolean hasLeft() {
            return left != null;
        }

        public boolean hasRight() {
            return right != null;
        }

        public boolean isNil() {
            return false;
        }

        public boolean isLeaf() {
            return left.isNil() && right.isNil();
        }

        public int updateHeight() {
            height = Math.max(left.height, right.height) + 1;
            return height;
        }

        protected Pair<ValuedRange, ValuedRange> updateRange(ValuedRange rng) {
            if (range == null) {
                range = rng;
                return Pair.empty();
            }
            if (rng.below(this.range)) {
                return Pair.of(rng, null);
            }
            if (rng.above(this.range)) {
                return Pair.of(null, rng);
            }
            if (rng.same(range)) {
                range.value += rng.value;
                return Pair.empty();
            }
            if (rng.contains(range)) {
                var newLeft = new ValuedRange(rng.start, this.range.start - 1, rng.value);
                var newRight = new ValuedRange(this.range.end + 1, rng.end, rng.value);
                this.range.value += rng.value;
                return Pair.of(newLeft, newRight);
            }
            if (range.contains(rng)) {
                var left = new ValuedRange(this.range.start, rng.start - 1, range.value);
                var right = new ValuedRange(rng.end + 1, this.range.end, range.value);
                this.range.value += rng.value;
                return Pair.of(left, right);
            }
            if (rng.start < this.range.start) {
                // rng tail overlaps with this range's head
                // inserts the non-overlapping tail of this range to the right
                var newRight = new ValuedRange(rng.end + 1, range.end, range.value);
                range.end = rng.end;
                range.value += rng.value;
                // reuses rng, only shrinking its tail
                rng.end = range.start - 1;
                return Pair.of(rng, newRight);
            }
            // Only case remaining is rng head overlapping with this range's tail
            var newLeft = new ValuedRange(this.range.start, rng.start - 1, range.value);
            range.start = rng.start;
            range.value += rng.value;
            // reuses rng, only shrinking its head
            rng.start = range.end + 1;
            return Pair.of(newLeft, rng);
        }

        private int balance() {
            return left.height - right.height;
        }

        private Case determineCase() {
            if (balance() > 1) {
                // left leaning
                if (left.right.height > left.left.height) {
                    return Case.LR;
                } else {
                    return Case.LL;
                }
            }
            if (balance() < -1) {
                if (right.left.height > right.right.height) {
                    return Case.RL;
                } else {
                    return Case.RR;
                }
            }
            return Case.NONE;
        }

        public Node insert(ValuedRange rng) {
            var res = updateRange(rng);
            var leftTask = CompletableFuture.completedFuture(left);
            var rightTask = CompletableFuture.completedFuture(right);
            if (res.hasLeft()) {
                leftTask = CompletableFuture.supplyAsync(() -> left.insert(res.left()));
            }
            if (res.hasRight()) {
                rightTask = CompletableFuture.supplyAsync(() -> right.insert(res.right()));
            }
            CompletableFuture.allOf(leftTask, rightTask).join();
            left = leftTask.join();
            right = rightTask.join();
            height = Math.max(left.height, right.height) + 1;

            if (balance() <= 1 && balance() >= -1) {
                updateHeight();
                return this; // Nodes are balanced, nothing needed
            }

            var cas = determineCase();

            return switch (cas) {
                case LL -> rotateRight();
                case LR -> {
                    left = left.rotateLeft();
                    yield rotateRight();
                }
                case RR -> rotateLeft();
                case RL -> {
                    right = right.rotateRight();
                    yield rotateLeft();
                }
                default -> throw new IllegalStateException("Could not correctly determine case");
            };

        }

        protected Node rotateLeft() {
            if (right.isNil()) {
                throw new IllegalStateException("Cannot promote NilNode");
            }
            var y = right;
            right = y.left;
            y.left = this;
            updateHeight();
            y.updateHeight();
            return y;
        }

        protected Node rotateRight() {
            if (left.isNil()) {
                throw new IllegalStateException("Cannot promote NilNode");
            }
            var y = left;
            left = y.right;
            y.right = this;
            updateHeight();
            y.updateHeight();
            return y;
        }

        protected Long findMaximum() {
            var maxLeft = CompletableFuture.supplyAsync(() -> left.findMaximum());
            var maxRight = CompletableFuture.supplyAsync(() -> right.findMaximum());

            CompletableFuture.allOf(maxLeft, maxRight).join();
            var mxChildren = Math.max(maxLeft.join(), maxRight.join());
            return Math.max(range.value, mxChildren);
        }

        public void toString(int treeHeight, int offset, ConcurrentMap<Integer, StringBuffer> lines) {
            var line = lines.get(treeHeight);
            var txt = CELL_FORMAT.formatted(range.toString());
            CompletableFuture<Void> leftTask = CompletableFuture.runAsync(() -> {});
            var rightTask = leftTask;
            if (!left.isNil()) {
                leftTask = CompletableFuture.runAsync(() -> left.toString(treeHeight + 1, offset - CELL_WIDTH * 2, lines));
            }
            if (!right.isNil()) {
                rightTask = CompletableFuture.runAsync(() -> right.toString(treeHeight + 1, offset + CELL_WIDTH * 2, lines));
            }

            line.replace(offset, txt.length() + offset, txt);
            CompletableFuture.allOf(leftTask, rightTask).join();
        }
    }

    protected static class NilNode extends Node {
        public NilNode() {
            super();
            height = 0;
        }

        public boolean isNil() {
            return true;
        }

        @Override
        public Node insert(ValuedRange range) {
            return new Node(range);
        }

        @Override
        protected Node rotateLeft() {
            throw new IllegalStateException("Cannot rotate NilNode");
        }

        @Override
        protected Node rotateRight() {
            return rotateLeft();
        }

        @Override
        public Long findMaximum() {
            return Long.MIN_VALUE;
        }
    }

    private Node root;

    public RangeAvlTree(ValuedRange rng) {
        root = new Node(rng);
    }

    public RangeAvlTree() {
        root = NIL;
    }

    public void insert(ValuedRange rng) {
        root = root.insert(rng);
    }

    public Long findMaximum() {
        return root.findMaximum();
    }

    public String toString() {
        var treeHeight = root.height;
        if (treeHeight <= 0) {
            return "Empty tree";
        }
        int firstOffset = Long.valueOf(Math.round(Math.pow(2.0f, (float) treeHeight) / 2.0f)).intValue() * CELL_WIDTH/2;
        var map = new ConcurrentHashMap<Integer, StringBuffer>();
        for (int i = 0; i < treeHeight; i++) {
            map.put(i, new StringBuffer("-".repeat(firstOffset * 2)));
        }
        root.toString(0, firstOffset, map);
        var sb = new StringBuilder();
        for (int i = 0; i < treeHeight; i++) {
            sb.append(map.getOrDefault(i, new StringBuffer())).append("\n");
        }
        return sb.toString();
    }

}

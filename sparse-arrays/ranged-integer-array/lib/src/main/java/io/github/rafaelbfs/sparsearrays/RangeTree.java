package io.github.rafaelbfs.sparsearrays;

import io.github.rafaelbfs.sparsearrays.util.Pair;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RangeTree {
    private static final NilNode NIL = new NilNode();
    protected enum Color {
        RED, BLACK;
        protected Color opposite() {
            return this.equals(Color.RED) ? Color.BLACK : Color.RED;
        }
    };
    protected enum Direction {LEFT, RIGHT};
    protected enum Correction {NONE, ROTATE_LEFT, ROTATE_RIGHT, RECOLOR, CHILD_ADDED, REVALIDATE, VIOLATION_DETECTED,
        REPLACE_CHILD, NO_CORRECTION_NEEDED;

        protected Pair<Correction, Node> pair(Node node) {
            return Pair.of(this, node);
        }
    };
    protected  static final EnumSet<Correction> REPLACE_CHILD_ACTIONS =
            EnumSet.of(Correction.REPLACE_CHILD, Correction.VIOLATION_DETECTED, Correction.NO_CORRECTION_NEEDED);


    private static class Node {
        protected ValuedRange range;
        protected Color color;
        protected Node left;
        protected Node right;

        public Node(ValuedRange range) {
            this.range = range;
            this.left = NIL;
            this.right = NIL;
            this.color = Color.RED;
        }

        protected Node() {
            this(null);
        }

        public boolean isRed() {
            return Color.RED.equals(color);
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

        public Correction recolorChildren(Node child, Direction side) {
            color = child.color.opposite();
            if (side.equals(Direction.LEFT)) {
                right.color = left.color;
            } else {
                left.color = right.color;
            }
            return Correction.RECOLOR;
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
                var newLeft = new ValuedRange(rng.start, this.range.start - 1 , rng.value);
                var newRight = new ValuedRange(this.range.end + 1, rng.end, rng.value);
                this.range.value += rng.value;
                return Pair.of(newLeft, newRight);
            }
            if (range.contains(rng)) {
                var left = new ValuedRange(this.range.start, rng.start - 1 , range.value);
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
            var newLeft = new ValuedRange(this.range.start, rng.start - 1 , range.value);
            range.start = rng.start;
            range.value += rng.value;
            // reuses rng, only shrinking its head
            rng.start = range.end + 1;
            return Pair.of(newLeft, rng);
        }

        public Pair<Correction, Node> insert(ValuedRange rng, Node parent) {
            var res = updateRange(rng);
            var leftTask = CompletableFuture.completedFuture(Pair.of(Correction.NONE, (Node) null));
            var rightTask = leftTask;
            if (res.hasLeft()) {
                leftTask = CompletableFuture.supplyAsync(() -> this.left.insert(res.left(), this))
                        .thenApplyAsync(this::postInsert);
            }
            if (res.hasRight()) {
                rightTask = CompletableFuture.supplyAsync(() -> this.right.insert(res.right(), this))
                        .thenApplyAsync(this::postInsert);
            }
            var resLeft = leftTask.join();
            if (REPLACE_CHILD_ACTIONS.contains(resLeft.fst()) && resLeft.snd() != null) {
                left = resLeft.snd();
            }
            var resRight = rightTask.join();
            if (REPLACE_CHILD_ACTIONS.contains(resRight.fst()) && resRight.snd() != null) {
                right = resRight.snd();
            }
            if (Correction.NO_CORRECTION_NEEDED.equals(resLeft.fst())
                    && resLeft.fst().equals(resRight.fst())) {
                // Correction.NONE instructs parent to ignore this child
                return Pair.of(Correction.NONE, null);
            }

            return Correction.REVALIDATE.pair(this);
        }

        private Pair<Correction, Node> rotateLeft() {
            var y = right;
            right = y.left;
            y.left = this;
            return Pair.of(Correction.REPLACE_CHILD, y);
        }

        private Pair<Correction, Node> rotateRight() {
            var y = left;
            left = y.right;
            y.right = this;
            return Pair.of(Correction.REPLACE_CHILD, y);
        }

        private Pair<Correction, Node> postInsert(Pair<Correction, Node> insertResult) {
            if (Correction.CHILD_ADDED.equals(insertResult.fst()) && isRed()) {
                return Pair.of(Correction.VIOLATION_DETECTED, insertResult.snd());
            } else if (Correction.CHILD_ADDED.equals(insertResult.fst())) {
                return Pair.of(Correction.NO_CORRECTION_NEEDED, insertResult.snd());
            }
            return insertResult;
        }

        public Pair<Correction, Node> validate(Node parent, Direction side, Correction previousAction) {
            var node = side.equals(Direction.LEFT) ? left : right;
            var thisSide = parent.left == this ? Direction.LEFT : Direction.RIGHT;
            if (Color.BLACK.equals(color) && Correction.CHILD_ADDED.equals(previousAction)) {
                // No correction needed
                return Pair.of(Correction.NONE, node);
            }
            if (Color.BLACK.equals(node.color) && isRed()) {
                // there may be violations upstream
                return Pair.of(Correction.REVALIDATE, this);
            }
            // this node is red, retrieve "uncle" of the new node (this node's sibling)
            var uncle = parent.left == this ? parent.right : parent.left;
            if (uncle.isRed()) {
                // Recolor upwards in the call chain
                color = Color.BLACK;
                // due to synchronization issues, the grandparent (this node's parent) must be the one to recolor
                // the uncle, as it might be waiting the uncle to complete a task
                return new Pair<>(Correction.RECOLOR, this);
            }
            // past this point, uncle is black
            // Left-Left case: newly inserted node is a left child and this is a left child -> rotate right on grandparent
            if (Direction.LEFT.equals(side) && thisSide.equals(side)) {
                return new Pair<>(Correction.ROTATE_RIGHT, parent);
            }
            // Right-right case: newly inserted node is a right child and this is a right child too ->
            // rotate left on grandparent
            if (Direction.RIGHT.equals(side) && thisSide.equals(side)) {
                return new Pair<>(Correction.ROTATE_LEFT, parent);
            }
            // Triangle cases (L-R/R-L): when child - parent (this) - grandparent hierarchy forms a triangle
            if (Direction.RIGHT.equals(side)) {
                // R-L case: node is the right child of a left child (this) -> rotate this to the right
                return new Pair<>(Correction.ROTATE_RIGHT, this);
            }
            // The only case remaining is R-L
            return new Pair<>(Correction.ROTATE_LEFT, this);
        }
    }

    private static class NilNode extends Node {
        public NilNode() {
            super();
            color = Color.BLACK;
        }

        public boolean isNil() {
            return true;
        }

        @Override
        public Pair<Correction, Node> insert(ValuedRange range, Node parent) {
            return Pair.of(Correction.CHILD_ADDED, new Node(range));
        }
    }

    // recursive left and right rotation red-black tree

    private static class RootNode extends Node {
        public RootNode() {
            super();
            color = Color.BLACK;
        }

        public void insert(ValuedRange rng) {
            if (range == null) {
                range = rng;
            }
            var res = updateRange(rng);
            if (res.hasLeft()) {
                left.insert(res.left(), this);
            }
            if (res.hasRight()) {
                right.insert(res.right(), this);
            }
        }
    }




    //private final ValuedRange root;

}

package com.rafael.demos.montyhall;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Utility {
  public static final Random RAND = new Random();

  public static int positiveMod(int number, int divisor) {
    return Math.abs(number % divisor);
  }

  public static int zeroBasedRandom(int bound) {
    return positiveMod(RAND.nextInt(), bound);
  }

  public static int randomExcluding(Set<Integer> excluded, int bound) {
    var rem = IntStream.range(0, bound).filter(i -> !excluded.contains(i))
        .toArray();
    check(() -> rem.length > 0, "No remaining options to choose");
    return rem.length > 1 ? rem[zeroBasedRandom(rem.length)] : rem[0];
  }

  public static Set<Integer> setOf(int e1, int e2) {
    return e1 == e2 ? Set.of(e1) : Set.of(e1, e2);
  }

  public static void check(Supplier<Boolean> boolExpr, String errorMsg) {
    if (!boolExpr.get()) {
      throw new IllegalStateException(errorMsg);
    }
  }
}

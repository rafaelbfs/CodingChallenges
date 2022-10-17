package com.rafael.demos.montyhall.states;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Game {
  private static final Random RAND = new Random();
  private static final int NUMBER_OF_BOXES = 3;
  public record Match(int initialChoice, int prizeBox,
                      boolean change) {
  }

  public record Outcome(Match match, boolean won) {
    Outcome(Match s) {
      this(s, (!s.change && s.initialChoice == s.prizeBox) ||
          (s.change && s.initialChoice != s.prizeBox));
    }
  }

  public static List<Match> generate(int amount) {
   var list = new ArrayList<Match>(amount + 8);
   for (int i = 0; i < amount; i++) {
     list.add(random());
   }
   return list;
  }

  public static Match random() {
    return new Match(positiveMod(RAND.nextInt(), NUMBER_OF_BOXES),
        positiveMod(RAND.nextInt(), NUMBER_OF_BOXES),
        (RAND.nextInt() & 1) == 0);
  }

  public static int positiveMod(int nr, int divisor) {
    return Math.abs(nr % divisor);
  }
}

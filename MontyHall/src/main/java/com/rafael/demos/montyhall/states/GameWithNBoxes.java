package com.rafael.demos.montyhall.states;

import static com.rafael.demos.montyhall.Utility.*;

import com.rafael.demos.montyhall.states.Game.Match;
import com.rafael.demos.montyhall.states.Game.Outcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameWithNBoxes {

  public record NGame(int numberOfBoxes, int initialChoice,
                      int boxWithPrize, int openedByHost,
                      int newChoice) {
    public boolean changed() {
      return initialChoice != newChoice;
    }
  }

  public record Outcome(NGame match, boolean won) {
    public Outcome(NGame match) {
      this(match, match.newChoice == match.boxWithPrize);
    }
  }

  public static NGame make(int numberOfBoxes) {
    var firstChoice = zeroBasedRandom(numberOfBoxes);
    var prizeBox = zeroBasedRandom(numberOfBoxes);
    var openedByHost = randomExcluding(setOf(firstChoice, prizeBox),
        numberOfBoxes);
    return new NGame(numberOfBoxes, firstChoice, prizeBox, openedByHost,
        randomExcluding(Set.of(openedByHost), numberOfBoxes));
  }

  public static List<NGame> generate(int amount, int boxes) {
    check(() -> boxes > 2,
        "Number of boxes must be higher than 2!");
      var list = new ArrayList<NGame>(amount + 8);
      for (int i = 0; i < amount; i++) {
        list.add(make(boxes));
      }
      return list;
  }
}

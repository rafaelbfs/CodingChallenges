package com.rafael.demos.montyhall;

import com.rafael.demos.montyhall.states.Permutations;

public class Main {

  public static void main(String... args) {
    var permutations = Permutations.generatePermutations();
    //Permutations.printGames(permutations);
    var summary = Permutations.summary(permutations);
    var summaryByStrategy = Permutations.summarizeByChoice(summary);

    Permutations.printOverallStatistics(summaryByStrategy);
  }

}

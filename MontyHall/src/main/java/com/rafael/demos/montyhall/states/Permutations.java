package com.rafael.demos.montyhall.states;

import com.rafael.demos.montyhall.states.Game.Outcome;
import com.rafael.demos.montyhall.states.Game.PlayerInputs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Permutations {
  public static Map<PlayerInputs, List<Game>> generatePermutations() {
    var possiblePlayersChoices = IntStream.range(1, 4)
        .mapToObj(firstBox ->
            IntStream.range(0, 2)
                .mapToObj(choice -> new PlayerInputs(firstBox, Boolean.valueOf(choice != 0)))
                .collect(Collectors.toList())).flatMap(List::stream);
    return possiblePlayersChoices.map(Permutations::generateMatchesForPlayerChoices)
        .flatMap(List::stream).collect(Collectors.groupingBy(Game::getPlayerInputs));
  }

  public static List<SummaryWithPlayerStrategy> summary(Map<PlayerInputs, List<Game>> matches) {
    return matches.entrySet().stream().map(entry -> new SummaryWithPlayerStrategy(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  public static Map<Boolean, Summary> summarizeByChoice(List<SummaryWithPlayerStrategy> summaries) {
    var groupedBySecondChoice =
        summaries.stream().collect(Collectors.partitioningBy(s -> s.inputs.changeBox));
    var switchedBox = concatenate(groupedBySecondChoice.get(true));
    var keptBox = concatenate(groupedBySecondChoice.get(false));
    return Map.of(true, switchedBox, false, keptBox);
  }

  public static void printOverallStatistics(Map<Boolean, Summary> summaryByPlayerSwitchingBox) {
    System.out.println("\n\nSummary:");
    System.out.println("By remaining with same initial choice: %s"
        .formatted(summaryByPlayerSwitchingBox.get(false)));
    System.out.println("By switching box: %s".formatted(summaryByPlayerSwitchingBox.get(true)));

    double ratioWhenKeepingChoice = summaryByPlayerSwitchingBox.get(false).getRatio();
    double ratioWhenSwitchingBox = summaryByPlayerSwitchingBox.get(true).getRatio();

    System.out.println("%n%nTherefore it is more worth to: %s"
        .formatted(ratioWhenSwitchingBox > ratioWhenKeepingChoice ?
            "Switch box" : "Maintain choice"));


  }

  public static List<Game> generateMatchesForPlayerChoices(PlayerInputs playerInputs) {
    return IntStream.range(1, 4).mapToObj(prizedBox -> IntStream.range(1, 4)
        .filter(hostOpens -> playerInputs.firstChoice != hostOpens && prizedBox != hostOpens)
        .mapToObj(hostOpens -> new Game(playerInputs.firstChoice, prizedBox, hostOpens,
            playerInputs.changeBox)).collect(Collectors.toList())).flatMap(List::stream)
        .collect(Collectors.toList());
  }

  public static void printGames(Map<PlayerInputs, List<Game>> games) {
    games.entrySet().stream()
        .forEach(e -> {
          System.out.println("%n%nWith player choosing box %s".formatted(e.getKey().firstChoice));
          System.out.println(Game.printHeader());
          System.out.println(e.getValue().stream().map(Game::toString)
              .collect(Collectors.joining("\n")));
        });
  }

  public static class SummaryWithPlayerStrategy extends Summary {
    private final PlayerInputs inputs;

    public SummaryWithPlayerStrategy(PlayerInputs inputs, List<Game> allGames) {
      super(allGames);
      this.inputs = inputs;
    }
  }

  private static Summary concatenate(List<SummaryWithPlayerStrategy> summaries) {
    var allGames = summaries.stream()
        .flatMap(s -> Stream.of(s.victorious, s.lost).flatMap(List::stream));
    return new Summary(allGames);
  }

  public static class Summary {
    protected final List<Game> victorious;
    protected final List<Game> lost;

    public Summary(List<Game> allGames) {
      this(allGames.stream());
    }

    public Summary(Stream<Game> allGames) {
      var partitioned = allGames.distinct()
          .collect(Collectors
          .partitioningBy(g -> Outcome.VICTORY.equals(g.calculateOutcome()))) ;
      victorious = partitioned.get(true);
      lost = partitioned.get(false);
    }

    public double getRatio() {
      return Double.valueOf(victorious.size()) / Double.valueOf(victorious.size() + lost.size());
    }

    @Override
    public String toString() {
      return ("Player wins %d matches out of %d combinations of her initial choice and the"
          + " box containing the prize, ratio = %f")
          .formatted(victorious.size(), victorious.size() + lost.size(), getRatio());
    }
  }

}

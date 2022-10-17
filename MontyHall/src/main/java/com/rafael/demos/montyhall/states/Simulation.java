package com.rafael.demos.montyhall.states;

import com.rafael.demos.montyhall.states.Game.Outcome;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Simulation {
  final long total;
  final Map<Outcome, Long> outcomesGroups;
  final Map<Boolean, Count> victoriesByDecision;

  public Simulation(Integer totalGames) {
    total = totalGames.longValue();
    var outcomes = Game.generate(totalGames).stream()
        .map(Outcome::new);
    outcomesGroups = outcomes.collect(Collectors.groupingBy(o -> o,
        Collectors.counting()));

    Map<Boolean, List<Entry<Outcome, Long>>> m = outcomesGroups.entrySet().stream()
        .collect(Collectors.partitioningBy(e -> e.getKey().match().change()));
    victoriesByDecision = m.entrySet().stream().map(e -> {
      Long victories = e.getValue().stream().filter(v -> v.getKey().won())
          .collect(Collectors.summingLong(Entry::getValue));
      Long total = e.getValue().stream()
          .collect(Collectors.summingLong(Entry::getValue));
      return Map.entry(e.getKey(), new Count(victories, total));
    }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  @Override
  public String toString() {
    Long vsByChanging = victoriesByDecision.get(true).victories;
    Long totalChanged = victoriesByDecision.get(true).total;
    Long vsByRemaining = victoriesByDecision.get(false).victories;
    Long totalKept = victoriesByDecision.get(false).total;
    return String.format("Of %d matches %n %d out of %d were won by "
        + "changing the door rate = %.2f%% %n "
        + "%d out of %d were won by remaining with the "
        + "initial choice (%.2f%%)", total, vsByChanging, totalChanged,
            vsByChanging.doubleValue() / totalChanged.doubleValue() * 100.00,
            vsByRemaining, totalKept,
            vsByRemaining.doubleValue() / totalKept.doubleValue() * 100.00);
  }

  public record Count(long victories, long total) {

  }

}

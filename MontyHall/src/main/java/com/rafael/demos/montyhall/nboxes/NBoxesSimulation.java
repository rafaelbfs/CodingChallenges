package com.rafael.demos.montyhall.nboxes;

import com.rafael.demos.montyhall.states.GameWithNBoxes;
import static com.rafael.demos.montyhall.states.GameWithNBoxes.Outcome;
import com.rafael.demos.montyhall.states.Simulation;
import com.rafael.demos.montyhall.states.Simulation.Count;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NBoxesSimulation {

  final int numberOfBoxes;
  final long total;
  final Map<Outcome, Long> outcomesGroups;
  final Map<Boolean, Count> victoriesByDecision;

  public NBoxesSimulation(Integer totalGames, Integer numberOfBoxes) {
    this.numberOfBoxes = numberOfBoxes;
    total = totalGames.longValue();
    var outcomes = GameWithNBoxes.generate(totalGames, numberOfBoxes)
        .stream().map(Outcome::new);
    outcomesGroups = outcomes.collect(Collectors.groupingBy(o -> o,
        Collectors.counting()));

    Map<Boolean, List<Entry<Outcome, Long>>> m = outcomesGroups.entrySet().stream()
        .collect(Collectors.partitioningBy(e -> e.getKey().match().changed()));
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
    Long vsByChanging = victoriesByDecision.get(true).victories();
    Long totalChanged = victoriesByDecision.get(true).total();
    Long vsByRemaining = victoriesByDecision.get(false).victories();
    Long totalKept = victoriesByDecision.get(false).total();
    return String.format("Of %d matches for a game played with %d boxes %n"
            + "%d out of %d were won by changing the door, ie. %.2f%% %n "
            + "%d out of %d were won by keeping the initial choice (%.2f%%)",
        total, numberOfBoxes, vsByChanging, totalChanged,
        vsByChanging.doubleValue() / totalChanged.doubleValue() * 100.00,
        vsByRemaining, totalKept,
        vsByRemaining.doubleValue() / totalKept.doubleValue() * 100.00);
  }

  public record Count(long victories, long total) {

  }

}

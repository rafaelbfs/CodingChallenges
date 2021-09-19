package com.rafael.demos.montyhall.states;

import java.util.Objects;
import java.util.Optional;

public class Game {
  private static final String HEADER_PRIZED_BOX = "Box with Prize",
  PLAYER_CHOICE = " Player chose", HOST_OPENS = " Host Opens", OUTCOME = " Outcome";

  private final int initiallyChosenBox;
  private final PlayerChoice playerChoice;
  private final int boxWithPrize;
  private final Box[] boxes = new Box[3];
  private final int boxOpenedByHost;

  public Game(int chosenBox, int boxWithPrize, int boxOpenedByHost, boolean playerSwitches) {
    this.initiallyChosenBox = --chosenBox;
    this.playerChoice = playerSwitches ? PlayerChoice.SWITCH_BOX : PlayerChoice.SAME_BOX;
    this.boxWithPrize = --boxWithPrize;
    for (int i = 0; i < 3; i++) {
      boxes[i] = new Box(i == this.boxWithPrize ? BoxContents.MONEY : BoxContents.NOTHING);
    }
    this.boxOpenedByHost = boxOpenedByHost - 1;

    if (this.boxOpenedByHost == this.initiallyChosenBox ||
        BoxContents.MONEY.equals(boxes[this.boxOpenedByHost])) {
      new IllegalStateException("Host cannot open a box chosen "
          + "by player or with prize");
    }
  }

  @Override
  /**
   * This method has been overriden to ignore which box the host opened as it won't
   * matter because when the player chooses initially an empty box the host will be
   * bound to open the only other which doesn't have prize.
   * The only way the host has two options is when the player iniatially chooses the prized box
   * and in this case whatever box the host opens will not determine the outcome of the match as
   * it will be solely determined by wether the player chooses to change or remain with same box.
   *
   * Therefore the 'boxOpenedByHost' variable is ignored in the equality comparison in the final
   * counting, so that an event which is technically the same is not counted twice.
   *
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Game game = (Game) other;
    return initiallyChosenBox == game.initiallyChosenBox && boxWithPrize == game.boxWithPrize
        && playerChoice == game.playerChoice;
  }

  @Override
  public int hashCode() {
    return Objects.hash(initiallyChosenBox, playerChoice, boxWithPrize);
  }

  public boolean playerSwitched() {
    return PlayerChoice.SWITCH_BOX.equals(playerChoice);
  }

  public Outcome calculateOutcome() {
    var playerChosePrizedBoxInitially = boxes[initiallyChosenBox].hasPrize();
    var playerKeptInitialChoice = PlayerChoice.SAME_BOX.equals(playerChoice);
    // The only two ways the player can win is when he chooses the prized box first and keeps his choice
    // OR chooses an empty box and switches to another which will inevitably be the prized one as
    // the host cannot open a prized box. Those conditions are satisfied if the values of
    // the two variables above are the same.
    return playerChosePrizedBoxInitially == playerKeptInitialChoice ?
        Outcome.VICTORY : Outcome.DEFEAT;
  }

  public String printColumn() {
    var prizedBox = String.format(">%" + HEADER_PRIZED_BOX.length() + "d |", boxWithPrize + 1);
    var hostOpenedBox = String.format("%" + HOST_OPENS.length() + "s |", "Box " +
        (boxOpenedByHost + 1));
    var playerChose = String.format("%" + PLAYER_CHOICE.length() + "s |",
        PlayerChoice.SWITCH_BOX.equals(playerChoice) ? "To switch" : "To remain");
    var outcome = String.format("%" + OUTCOME.length() + "s ",
        calculateOutcome().name().toLowerCase());
    return prizedBox + hostOpenedBox + playerChose + outcome;
  }

  public static String printHeader() {
    return String.format("|%s |%s |%s |%s", HEADER_PRIZED_BOX, HOST_OPENS, PLAYER_CHOICE, OUTCOME);
  }

  @Override
  public String toString() {
    return printColumn();
  }

  public PlayerInputs getPlayerInputs() {
    return new PlayerInputs(initiallyChosenBox + 1,
        PlayerChoice.SWITCH_BOX.equals(playerChoice));
  }

  public static class PlayerInputs implements Comparable<PlayerInputs> {
    protected final int firstChoice;
    protected final boolean changeBox;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PlayerInputs that = (PlayerInputs) o;
      return firstChoice == that.firstChoice && changeBox == that.changeBox;
    }

    @Override
    public int hashCode() {
      return Objects.hash(firstChoice, changeBox);
    }

    public PlayerInputs(int firstChoice, boolean changeBox) {
      this.firstChoice = firstChoice;
      this.changeBox = changeBox;
    }

    @Override
    public int compareTo(PlayerInputs o) {
      return o.firstChoice != firstChoice ? Integer.compare(firstChoice, o.firstChoice) :
          Boolean.compare(changeBox, o.changeBox);
    }
  }

  public class Box {
    private final BoxContents contents;

    public boolean hasPrize() {
      return BoxContents.MONEY.equals(contents);
    }

    public Box(BoxContents contents) {
      this.contents = contents;
    }
  }

  public enum Outcome {
    DEFEAT, VICTORY;
  }

  public enum BoxContents {
    NOTHING, MONEY;
  }

  public enum PlayerChoice {
    SWITCH_BOX, SAME_BOX;
  }
}

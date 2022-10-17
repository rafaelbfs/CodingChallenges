package com.rafael.demos.montyhall;

import com.rafael.demos.montyhall.nboxes.NBoxesSimulation;
import com.rafael.demos.montyhall.states.Simulation;

public class Main {

  public static void main(String... args) {
    var time = System.currentTimeMillis();
    var s = new NBoxesSimulation(500000, 8);
    time = System.currentTimeMillis() - time;
    System.out.println("Simulation took %d ms".formatted(time));
    System.out.println(s);
  }

}

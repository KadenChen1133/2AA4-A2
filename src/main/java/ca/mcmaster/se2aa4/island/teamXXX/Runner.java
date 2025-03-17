package ca.mcmaster.se2aa4.island.teamXXX;

import static eu.ace_design.island.runner.Runner.run;
import java.io.File;

public class Runner {

    // Main method checks for the required filename argument.
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Runner <filename>");
            System.exit(1);
        }
        String filename = args[0];
        try {
            run(Explorer.class)
                    .exploring(new File(filename))
                    .withSeed(42L)
                    .startingAt(1, 1, "EAST")
                    .backBefore(7000)
                    .withCrew(5)
                    .collecting(1000, "WOOD")
                    .storingInto("./outputs")
                    .withName("Island")
                    .fire();
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}



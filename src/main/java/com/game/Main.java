package com.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.model.GameConfig;
import com.game.core.Game;
import com.game.model.GameResult;
import java.util.*;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        try {
            var config = parseArguments(args);
            var mapper = new ObjectMapper();

            var gameConfig = mapper.readValue(Path.of(config.configPath()).toFile(), GameConfig.class);

            var game = new Game(gameConfig);
            var result = game.play(config.bettingAmount());

            System.out.println(mapper.writeValueAsString(result));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static record CLIConfig(String configPath, double bettingAmount) {}

    private static CLIConfig parseArguments(String[] args) {
        String configPath = null;
        double bettingAmount = 0;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config" -> {
                    if (i + 1 < args.length) configPath = args[++i];
                }
                case "--betting-amount" -> {
                    if (i + 1 < args.length) bettingAmount = Double.parseDouble(args[++i]);
                }
            }
        }

        if (configPath == null || bettingAmount <= 0) {
            throw new IllegalArgumentException("""
                Invalid arguments.
                Usage: java -jar scratch-game.jar --config config.json --betting-amount 100
                """);
        }

        return new CLIConfig(configPath, bettingAmount);
    }
}
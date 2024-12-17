package com.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.model.GameConfig;
import com.game.core.Game;
import com.game.model.GameResult;

import java.nio.file.Path;
import java.io.IOException;

public class Main {
    // Make record public for testing
    public static record CLIConfig(String configPath, double bettingAmount) {}

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    static GameResult run(String[] args) throws IOException {
        var config = parseArguments(args);
        var mapper = new ObjectMapper();
        var configFile = Path.of(config.configPath()).toFile();

        if (!configFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + config.configPath());
        }

        var gameConfig = mapper.readValue(configFile, GameConfig.class);
        var game = new Game(gameConfig);
        var result = game.play(config.bettingAmount());

        System.out.println(mapper.writeValueAsString(result));
        return result;
    }

    static CLIConfig parseArguments(String[] args) {
        String configPath = null;
        double bettingAmount = 0;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config" -> {
                    if (i + 1 < args.length) configPath = args[++i];
                }
                case "--betting-amount" -> {
                    if (i + 1 < args.length) {
                        try {
                            bettingAmount = Double.parseDouble(args[++i]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid betting amount: " + args[i]);
                        }
                    }
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
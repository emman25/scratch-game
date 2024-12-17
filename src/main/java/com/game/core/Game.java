package com.game.core;

import com.game.model.*;
import java.util.*;
import java.security.SecureRandom;

public class Game {
    private final GameConfig config;
    private final Random random;
    private static final double BONUS_SYMBOL_PROBABILITY = 0.2;

    public Game(GameConfig config) {
        this.config = config;
        this.random = new SecureRandom();
    }

    public GameResult play(double betAmount) {
        if (betAmount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }

        var matrix = generateMatrix();
        var winningCombinations = findWinningCombinations(matrix);
        var bonusSymbol = findBonusSymbol(matrix);
        var reward = calculateReward(betAmount, winningCombinations, bonusSymbol);

        return new GameResult(matrix, reward, winningCombinations, bonusSymbol);
    }

    protected double calculateReward(double betAmount,
                                     Map<String, List<String>> winningCombinations,
                                     String bonusSymbol) {
        if (winningCombinations.isEmpty()) {
            return 0;
        }

        double totalReward = 0;

        // Calculate for each winning symbol
        for (var entry : winningCombinations.entrySet()) {
            String symbol = entry.getKey();
            List<String> combinations = entry.getValue();

            // Get symbol base multiplier
            double symbolMultiplier = config.symbols().get(symbol).rewardMultiplier();

            // Calculate base symbol reward
            double symbolReward = betAmount * symbolMultiplier;

            // Apply combination multipliers one by one
            for (String combinationName : combinations) {
                double combinationMultiplier = config.winCombinations()
                        .get(combinationName)
                        .rewardMultiplier();
                symbolReward *= combinationMultiplier;
            }

            totalReward += symbolReward;
        }

        // Apply bonus symbol effect if present and there are wins
        if (bonusSymbol != null && totalReward > 0) {
            Symbol bonus = config.symbols().get(bonusSymbol);

            switch (bonus.impact()) {
                case "multiply_reward" -> {
                    totalReward *= bonus.rewardMultiplier();
                }
                case "extra_bonus" -> {
                    totalReward += bonus.extra();
                }
                case "miss" -> {} // No effect
            }
        }

        return totalReward;
    }

    private String[][] generateMatrix() {
        var matrix = new String[3][3]; // Fixed 3x3 matrix

        // Fill with standard symbols
        for (var prob : config.probabilities().standardSymbols()) {
            matrix[prob.row()][prob.column()] = selectSymbol(prob.symbols());
        }

        // Add random bonus symbol
        if (random.nextDouble() < BONUS_SYMBOL_PROBABILITY) {
            var row = random.nextInt(3);
            var col = random.nextInt(3);
            matrix[row][col] = selectSymbol(config.probabilities().bonusSymbols().symbols());
        }

        return matrix;
    }

    private String selectSymbol(Map<String, Integer> probabilities) {
        int total = probabilities.values().stream().mapToInt(Integer::intValue).sum();
        int rand = random.nextInt(total);

        int cumulative = 0;
        for (var entry : probabilities.entrySet()) {
            cumulative += entry.getValue();
            if (rand < cumulative) {
                return entry.getKey();
            }
        }

        return probabilities.keySet().iterator().next();
    }


    protected String findBonusSymbol(String[][] matrix) {
        for (var row : matrix) {
            for (var symbol : row) {
                var symbolConfig = config.symbols().get(symbol);
                if (symbolConfig != null && "bonus".equals(symbolConfig.type())) {
                    return symbol;
                }
            }
        }
        return null;
    }

    protected Map<String, List<String>> findWinningCombinations(String[][] matrix) {
        var result = new HashMap<String, List<String>>();
        var symbolCounts = countStandardSymbols(matrix);

        // For each symbol, get its best winning combination
        for (var entry : symbolCounts.entrySet()) {
            String symbol = entry.getKey();
            int count = entry.getValue();

            // Get all possible winning combinations for this symbol
            List<String> symbolWins = new ArrayList<>();

            // First try to find the highest count-based combination
            findBestSameSymbolCombination(count)
                    .ifPresent(symbolWins::add);

            // Then check for one line-based combination
            findBestLinearCombination(matrix, symbol)
                    .ifPresent(symbolWins::add);

            if (!symbolWins.isEmpty()) {
                result.put(symbol, symbolWins);
            }
        }

        return result;
    }

    private Optional<String> findBestSameSymbolCombination(int count) {
        return config.winCombinations().entrySet().stream()
                .filter(e -> "same_symbols".equals(e.getValue().when()))
                .filter(e -> count >= e.getValue().count())
                .max(Comparator.comparingInt(e -> e.getValue().count()))
                .map(Map.Entry::getKey);
    }

    private Optional<String> findBestLinearCombination(String[][] matrix, String symbol) {
        record CombinationValue(String name, WinCombination combination, double value) {}

        return config.winCombinations().entrySet().stream()
                .filter(e -> "linear_symbols".equals(e.getValue().when()))
                .map(e -> new CombinationValue(
                        e.getKey(),
                        e.getValue(),
                        e.getValue().rewardMultiplier()
                ))
                .filter(cv -> checkLinearCombination(matrix, symbol, cv.combination()))
                .max(Comparator.comparingDouble(CombinationValue::value))
                .map(CombinationValue::name);
    }

    private boolean checkLinearCombination(String[][] matrix, String symbol, WinCombination combination) {
        return combination.coveredAreas().stream().anyMatch(area -> {
            return area.stream().allMatch(pos -> {
                var coords = pos.split(":");
                var row = Integer.parseInt(coords[0]);
                var col = Integer.parseInt(coords[1]);

                if (row >= matrix.length || col >= matrix[0].length) {
                    return false;
                }

                return matrix[row][col].equals(symbol);
            });
        });
    }

    private Map<String, Integer> countStandardSymbols(String[][] matrix) {
        var symbolCount = new HashMap<String, Integer>();

        for (var row : matrix) {
            for (var symbol : row) {
                var symbolConfig = config.symbols().get(symbol);
                if (symbolConfig != null && "standard".equals(symbolConfig.type())) {
                    symbolCount.merge(symbol, 1, Integer::sum);
                }
            }
        }

        return symbolCount;
    }
}
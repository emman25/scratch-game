package com.game.core;

import com.game.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class GameTest {
    private GameConfig config;
    private Game game;

    @BeforeEach
    void setUp() {
        // Setup standard symbols
        var symbols = new HashMap<String, Symbol>();
        symbols.put("A", new Symbol(5.0, "standard", null, null));
        symbols.put("B", new Symbol(3.0, "standard", null, null));
        symbols.put("C", new Symbol(2.5, "standard", null, null));
        symbols.put("D", new Symbol(2.0, "standard", null, null));
        symbols.put("E", new Symbol(1.2, "standard", null, null));
        symbols.put("F", new Symbol(1.0, "standard", null, null));

        // Setup bonus symbols
        symbols.put("10x", new Symbol(10.0, "bonus", "multiply_reward", null));
        symbols.put("5x", new Symbol(5.0, "bonus", "multiply_reward", null));
        symbols.put("+1000", new Symbol(0.0, "bonus", "extra_bonus", 1000.0));
        symbols.put("+500", new Symbol(0.0, "bonus", "extra_bonus", 500.0));
        symbols.put("MISS", new Symbol(0.0, "bonus", "miss", null));

        // Setup probabilities
        var standardProbs = new ArrayList<StandardSymbolProbability>();
        var symbolProbs = Map.of(
                "A", 1, "B", 2, "C", 3,
                "D", 4, "E", 5, "F", 6
        );

        // Add all cell probabilities
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                standardProbs.add(new StandardSymbolProbability(j, i, symbolProbs));
            }
        }

        var bonusProbs = new BonusSymbolProbability(
                Map.of(
                        "10x", 1,
                        "5x", 2,
                        "+1000", 3,
                        "+500", 4,
                        "MISS", 5
                )
        );

        // Setup winning combinations
        var winCombinations = new HashMap<String, WinCombination>();

        // Same symbol combinations
        for (int i = 3; i <= 9; i++) {
            winCombinations.put("same_symbol_" + i + "_times",
                    new WinCombination(
                            getMultiplierForCount(i),
                            "same_symbols",
                            i,
                            "same_symbols",
                            null
                    )
            );
        }

        // Horizontal lines
        winCombinations.put("same_symbols_horizontally",
                new WinCombination(2.0, "linear_symbols", null, "horizontally_linear_symbols",
                        List.of(
                                List.of("0:0", "0:1", "0:2"),
                                List.of("1:0", "1:1", "1:2"),
                                List.of("2:0", "2:1", "2:2")
                        )
                )
        );

        // Vertical lines
        winCombinations.put("same_symbols_vertically",
                new WinCombination(2.0, "linear_symbols", null, "vertically_linear_symbols",
                        List.of(
                                List.of("0:0", "1:0", "2:0"),
                                List.of("0:1", "1:1", "2:1"),
                                List.of("0:2", "1:2", "2:2")
                        )
                )
        );

        // Diagonal lines
        winCombinations.put("same_symbols_diagonally_left_to_right",
                new WinCombination(5.0, "linear_symbols", null, "ltr_diagonally_linear_symbols",
                        List.of(List.of("0:0", "1:1", "2:2"))
                )
        );

        winCombinations.put("same_symbols_diagonally_right_to_left",
                new WinCombination(5.0, "linear_symbols", null, "rtl_diagonally_linear_symbols",
                        List.of(List.of("0:2", "1:1", "2:0"))
                )
        );

        config = new GameConfig(3, 3, symbols,
                new Probabilities(standardProbs, bonusProbs), winCombinations);
        game = new Game(config);
    }

    private double getMultiplierForCount(int count) {
        return switch (count) {
            case 3 -> 1.0;
            case 4 -> 1.5;
            case 5 -> 2.0;
            case 6 -> 3.0;
            case 7 -> 5.0;
            case 8 -> 10.0;
            case 9 -> 20.0;
            default -> 1.0;
        };
    }


    @Test
    @DisplayName("Should calculate reward for diagonal line")
    void shouldCalculateRewardForDiagonalLine() {
        var matrix = new String[][]{
                {"A", "B", "C"},
                {"D", "A", "F"},
                {"G", "H", "A"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var reward = game.calculateReward(100.0, winCombinations, null);

        assertEquals(2500.0, reward, "Reward should be 2500 (100 * 5.0 * 5.0)");
    }

    @Test
    @DisplayName("Should calculate reward for vertical line")
    void shouldCalculateRewardForVerticalLine() {
        var matrix = new String[][]{
                {"A", "B", "C"},
                {"A", "D", "E"},
                {"A", "F", "B"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var reward = game.calculateReward(100.0, winCombinations, null);

        assertEquals(1000.0, reward, "Reward should be 1000 (100 * 5.0 * 2.0)");
    }

    @Test
    @DisplayName("Should not apply bonus for losing game")
    void shouldNotApplyBonusForLosingGame() {
        var matrix = new String[][]{
                {"A", "B", "C"},
                {"D", "10x", "E"},
                {"F", "A", "B"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var bonus = game.findBonusSymbol(matrix);
        var reward = game.calculateReward(100.0, winCombinations, bonus);

        assertEquals(0.0, reward, "Reward should be 0 for losing game");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0})
    @DisplayName("Should reject invalid bet amounts")
    void shouldRejectInvalidBetAmounts(double betAmount) {
        assertThrows(IllegalArgumentException.class, () -> game.play(betAmount),
                "Should throw IllegalArgumentException for invalid bet amount: " + betAmount);
    }

    @Test
    @DisplayName("Should generate valid matrix")
    void shouldGenerateValidMatrix() {
        var result = game.play(100.0);
        var matrix = result.matrix();

        assertNotNull(matrix, "Matrix should not be null");
        assertEquals(3, matrix.length, "Matrix should have 3 rows");
        assertEquals(3, matrix[0].length, "Matrix should have 3 columns");

        // Check all cells contain valid symbols
        for (var row : matrix) {
            for (var symbol : row) {
                assertTrue(config.symbols().containsKey(symbol),
                        "Matrix contains invalid symbol: " + symbol);
            }
        }
    }

    @Test
    @DisplayName("Should calculate reward for three same symbols")
    void shouldCalculateRewardForThreeSameSymbols() {
        var matrix = new String[][]{
                {"A", "A", "A"},
                {"B", "C", "D"},
                {"E", "F", "B"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var reward = game.calculateReward(100.0, winCombinations, null);

        // Base reward = betAmount * symbolMultiplier * combinationMultiplier
        // 100 * 5.0 * 2.0 = 1000  (horizontal line multiplier is 2.0)
        assertEquals(1000.0, reward, "Reward should be 1000 (100 * 5.0 * 2.0 for horizontal line)");
    }

    @Test
    @DisplayName("Should apply 10x multiply bonus")
    void shouldApplyMultiplyBonus() {
        var matrix = new String[][]{
                {"A", "A", "A"},
                {"B", "10x", "D"},
                {"E", "F", "B"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var bonus = game.findBonusSymbol(matrix);
        var reward = game.calculateReward(100.0, winCombinations, bonus);

        // Base reward = betAmount * symbolMultiplier * combinationMultiplier * bonusMultiplier
        // 100 * 5.0 * 2.0 * 10 = 10000
        assertEquals(10000.0, reward, "Reward should be 10000 (100 * 5.0 * 2.0 * 10)");
    }

    @Test
    @DisplayName("Should apply +1000 extra bonus")
    void shouldApplyExtraBonus() {
        var matrix = new String[][]{
                {"A", "A", "A"},
                {"B", "+1000", "D"},
                {"E", "F", "B"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var bonus = game.findBonusSymbol(matrix);
        var reward = game.calculateReward(100.0, winCombinations, bonus);

        // Base reward = (betAmount * symbolMultiplier * combinationMultiplier) + extraBonus
        // (100 * 5.0 * 2.0) + 1000 = 2000
        assertEquals(2000.0, reward, "Reward should be 2000 ((100 * 5.0 * 2.0) + 1000)");
    }

    @Test
    @DisplayName("Should ignore MISS bonus")
    void shouldIgnoreMissBonus() {
        var matrix = new String[][]{
                {"A", "A", "A"},
                {"B", "MISS", "D"},
                {"E", "F", "B"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var bonus = game.findBonusSymbol(matrix);
        var reward = game.calculateReward(100.0, winCombinations, bonus);

        // Base reward = betAmount * symbolMultiplier * combinationMultiplier
        // 100 * 5.0 * 2.0 = 1000 (MISS has no effect)
        assertEquals(1000.0, reward, "Reward should be 1000 (100 * 5.0 * 2.0), MISS has no effect");
    }

    @Test
    @DisplayName("Should calculate reward for nine same symbols")
    void shouldCalculateRewardForNineSymbols() {
        var matrix = new String[][]{
                {"A", "A", "A"},
                {"A", "A", "A"},
                {"A", "A", "A"}
        };

        var winCombinations = game.findWinningCombinations(matrix);
        var reward = game.calculateReward(100.0, winCombinations, null);

        // For 9 same symbols:
        // Base reward = betAmount * symbolMultiplier * nine_symbols_multiplier
        // Additional rewards for horizontal and vertical lines are also applied
        // 100 * 5.0 * 20.0 = 50000
        assertEquals(50000.0, reward, "Reward should be 50000 (100 * 5.0 * 20.0 for nine same symbols)");
    }
}
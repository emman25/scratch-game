//package com.game.core;
//
//import com.game.model.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//import static org.junit.jupiter.api.Assertions.*;
//import java.util.*;
//
//class GameTest {
//    private GameConfig config;
//    private Game game;
//
//    @BeforeEach
//    void setUp() {
//        // Setup standard symbols
//        var symbols = new HashMap<String, Symbol>();
//        symbols.put("A", new Symbol(5.0, "standard", null, null));
//        symbols.put("B", new Symbol(3.0, "standard", null, null));
//        symbols.put("C", new Symbol(2.5, "standard", null, null));
//        symbols.put("D", new Symbol(2.0, "standard", null, null));
//        symbols.put("E", new Symbol(1.2, "standard", null, null));
//        symbols.put("F", new Symbol(1.0, "standard", null, null));
//
//        // Setup bonus symbols
//        symbols.put("10x", new Symbol(10.0, "bonus", "multiply_reward", null));
//        symbols.put("+1000", new Symbol(0.0, "bonus", "extra_bonus", 1000.0));
//        symbols.put("MISS", new Symbol(0.0, "bonus", "miss", null));
//
//        // Setup probabilities
//        var standardProbs = new ArrayList<StandardSymbolProbability>();
//        var symbolProbs = Map.of("A", 1, "B", 2, "C", 3, "D", 4, "E", 5, "F", 6);
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                standardProbs.add(new StandardSymbolProbability(j, i, symbolProbs));
//            }
//        }
//
//        var bonusProbs = new BonusSymbolProbability(
//                Map.of("10x", 1, "+1000", 3, "MISS", 4)
//        );
//
//        // Setup winning combinations
//        var winCombinations = new HashMap<String, WinCombination>();
//        winCombinations.put("same_symbol_3_times",
//                new WinCombination(1.0, "same_symbols", 3, "same_symbols", null));
//        winCombinations.put("same_symbol_4_times",
//                new WinCombination(1.5, "same_symbols", 4, "same_symbols", null));
//        winCombinations.put("same_symbol_5_times",
//                new WinCombination(2.0, "same_symbols", 5, "same_symbols", null));
//        winCombinations.put("same_symbols_horizontally",
//                new WinCombination(2.0, "linear_symbols", null, "horizontally",
//                        List.of(List.of("0:0", "0:1", "0:2"))));
//
//        config = new GameConfig(3, 3, symbols,
//                new Probabilities(standardProbs, bonusProbs), winCombinations);
//        game = new Game(config);
//    }
//
//    @Test
//    void shouldCalculateRewardForThreeSameSymbols() {
//        var matrix = new String[][]{
//                {"A", "A", "A"},
//                {"B", "C", "D"},
//                {"E", "F", "B"}
//        };
//
//        var winCombinations = game.findWinningCombinations(matrix);
//        var reward = game.calculateReward(100.0, winCombinations, null);
//
//        assertEquals(500.0, reward);  // 100 * 5.0 (A multiplier) * 1.0 (three same)
//    }
//
//    @Test
//    void shouldCalculateRewardWithMultiplyBonus() {
//        var matrix = new String[][]{
//                {"A", "A", "A"},
//                {"B", "10x", "D"},
//                {"E", "F", "B"}
//        };
//
//        var winCombinations = game.findWinningCombinations(matrix);
//        var bonusSymbol = game.findBonusSymbol(matrix);
//        var reward = game.calculateReward(100.0, winCombinations, bonusSymbol);
//
//        assertEquals(5000.0, reward);  // (100 * 5.0 * 1.0) * 10
//    }
//
//    @Test
//    void shouldCalculateRewardWithExtraBonus() {
//        var matrix = new String[][]{
//                {"A", "A", "A"},
//                {"B", "+1000", "D"},
//                {"E", "F", "B"}
//        };
//
//        var winCombinations = game.findWinningCombinations(matrix);
//        var bonusSymbol = game.findBonusSymbol(matrix);
//        var reward = game.calculateReward(100.0, winCombinations, bonusSymbol);
//
//        assertEquals(1500.0, reward);  // (100 * 5.0 * 1.0) + 1000
//    }
//
//    @Test
//    void shouldNotApplyBonusForLosingGame() {
//        var matrix = new String[][]{
//                {"A", "B", "C"},
//                {"D", "10x", "E"},
//                {"F", "A", "B"}
//        };
//
//        var winCombinations = game.findWinningCombinations(matrix);
//        var bonusSymbol = game.findBonusSymbol(matrix);
//        var reward = game.calculateReward(100.0, winCombinations, bonusSymbol);
//
//        assertEquals(0.0, reward);
//    }
//
//    @Test
//    void shouldCalculateRewardForHorizontalLine() {
//        var matrix = new String[][]{
//                {"A", "A", "A"},
//                {"B", "C", "D"},
//                {"E", "F", "B"}
//        };
//
//        var winCombinations = game.findWinningCombinations(matrix);
//        var reward = game.calculateReward(100.0, winCombinations, null);
//
//        assertEquals(1000.0, reward);  // 100 * 5.0 * 2.0 (horizontal)
//    }
//
//    @Test
//    void shouldHandleMultipleWinningCombinations() {
//        var matrix = new String[][]{
//                {"A", "A", "A"},
//                {"A", "A", "B"},
//                {"C", "D", "E"}
//        };
//
//        var winCombinations = game.findWinningCombinations(matrix);
//        var reward = game.calculateReward(100.0, winCombinations, null);
//
//        assertEquals(1000.0, reward);  // 100 * 5.0 * 2.0 (5 same symbols)
//    }
//
//    @ParameterizedTest
//    @ValueSource(doubles = {0.0, -1.0, -100.0})
//    void shouldRejectInvalidBetAmounts(double betAmount) {
//        assertThrows(IllegalArgumentException.class, () -> game.play(betAmount));
//    }
//
//    @Test
//    void shouldGenerateValidMatrix() {
//        var result = game.play(100.0);
//
//        assertNotNull(result.matrix());
//        assertEquals(3, result.matrix().length);
//        assertEquals(3, result.matrix()[0].length);
//
//        // All cells should contain valid symbols
//        for (var row : result.matrix()) {
//            for (var symbol : row) {
//                assertTrue(config.symbols().containsKey(symbol));
//            }
//        }
//    }
//}
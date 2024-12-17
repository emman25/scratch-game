package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record GameResult(
        String[][] matrix,
        double reward,

        @JsonProperty("applied_winning_combinations")
        Map<String, List<String>> appliedWinningCombinations,

        @JsonProperty("applied_bonus_symbol")
        String appliedBonusSymbol
) {}
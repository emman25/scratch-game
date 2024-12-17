package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record GameConfig(
        int columns,
        int rows,
        Map<String, Symbol> symbols,
        Probabilities probabilities,

        @JsonProperty("win_combinations")
        Map<String, WinCombination> winCombinations
) {}
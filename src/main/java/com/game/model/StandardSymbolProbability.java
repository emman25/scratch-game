package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record StandardSymbolProbability(
        int column,
        int row,
        Map<String, Integer> symbols
) {}
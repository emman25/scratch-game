package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WinCombination(
        @JsonProperty("reward_multiplier")
        double rewardMultiplier,

        String when,
        Integer count,
        String group,

        @JsonProperty("covered_areas")
        List<List<String>> coveredAreas
) {}
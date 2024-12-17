package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Symbol(
        @JsonProperty("reward_multiplier")
        double rewardMultiplier,

        String type,
        String impact,
        Double extra
) {}
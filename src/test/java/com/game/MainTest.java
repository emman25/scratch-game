package com.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Files;
import com.fasterxml.jackson.databind.ObjectMapper;

class MainTest {

    @Test
    void shouldParseValidArguments(@TempDir Path tempDir) throws Exception {
        // Create a minimal valid config
        var config = """
            {
                "columns": 3,
                "rows": 3,
                "symbols": {},
                "probabilities": {
                    "standard_symbols": [],
                    "bonus_symbols": {"symbols": {}}
                },
                "win_combinations": {}
            }
            """;

        var configPath = tempDir.resolve("valid-config.json");
        Files.writeString(configPath, config);

        var args = new String[]{"--config", configPath.toString(), "--betting-amount", "100"};
        var result = Main.parseArguments(args);

        assertEquals(configPath.toString(), result.configPath());
        assertEquals(100.0, result.bettingAmount());
    }

    @Test
    void shouldFailWithNoArguments() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> Main.parseArguments(new String[]{})
        );
        assertTrue(exception.getMessage().contains("Invalid arguments"));
    }

    @Test
    void shouldFailWithMissingConfigArgument() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> Main.parseArguments(new String[]{"--betting-amount", "100"})
        );
        assertTrue(exception.getMessage().contains("Invalid arguments"));
    }

    @Test
    void shouldFailWithMissingBettingAmount() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> Main.parseArguments(new String[]{"--config", "config.json"})
        );
        assertTrue(exception.getMessage().contains("Invalid arguments"));
    }

    @Test
    void shouldFailWithInvalidBetAmount(@TempDir Path tempDir) {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> Main.parseArguments(new String[]{"--config", "config.json", "--betting-amount", "-100"})
        );
        assertTrue(exception.getMessage().contains("Invalid arguments"));
    }

    @Test
    void shouldFailWithMissingConfigFile(@TempDir Path tempDir) {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> Main.run(new String[]{"--config", "nonexistent.json", "--betting-amount", "100"})
        );
        assertTrue(exception.getMessage().contains("Config file not found"));
    }

    @Test
    void shouldFailWithInvalidConfigFormat(@TempDir Path tempDir) throws Exception {
        var configPath = tempDir.resolve("invalid-config.json");
        Files.writeString(configPath, "invalid json content");

        assertThrows(
                Exception.class,
                () -> Main.run(new String[]{"--config", configPath.toString(), "--betting-amount", "100"})
        );
    }
}
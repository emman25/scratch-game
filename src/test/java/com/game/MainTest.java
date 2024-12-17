//package com.game;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import static org.junit.jupiter.api.Assertions.*;
//import java.nio.file.Path;
//import java.nio.file.Files;
//
//class MainTest {
//    @Test
//    void shouldFailWithNoArguments() {
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> Main.main(new String[]{}));
//        assertTrue(exception.getMessage().contains("Invalid arguments"));
//    }
//
//    @Test
//    void shouldFailWithInvalidBetAmount(@TempDir Path tempDir) throws Exception {
//        var configPath = tempDir.resolve("config.json");
//        Files.writeString(configPath, "{}");
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> Main.main(new String[]{"--config", configPath.toString(), "--betting-amount", "-100"}));
//        assertTrue(exception.getMessage().contains("Invalid arguments"));
//    }
//
//    @Test
//    void shouldFailWithMissingConfigFile() {
//        Exception exception = assertThrows(Exception.class,
//                () -> Main.main(new String[]{"--config", "nonexistent.json", "--betting-amount", "100"}));
//        assertTrue(exception.getMessage().contains("nonexistent.json"));
//    }
//}
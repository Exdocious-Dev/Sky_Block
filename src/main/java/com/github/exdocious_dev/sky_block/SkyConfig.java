package com.github.exdocious_dev.sky_block;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkyConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("sky_block");

    public static String islandSelected = "Skyblock Island";
    public static int offsetX = 3;
    public static int offsetY = 4;
    public static int offsetZ = 3;

    public static Path dir() { return FabricLoader.getInstance().getConfigDir().resolve("sky_block"); }
    public static Path islandsDir() { return dir().resolve("islands"); }

    public static void load() {
        try {
            Files.createDirectories(islandsDir());
            copyDefaultIsland();

            Path file = dir().resolve("Skyblock.properties");
            if (!Files.exists(file)) {
                Files.writeString(file, String.join(System.lineSeparator(),
                        "# Island Selected = name of a .nbt file in the islands folder (without .nbt)",
                        "# Offsets = the block inside the island that lands on world X0 / Y64 / Z0 (the spawn point)",
                        "Island Selected = Skyblock Island",
                        "Island X offset = 3",
                        "Island Y offset = 4",
                        "Island Z offset = 3",
                        ""));
            }
            for (String line : Files.readAllLines(file)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(eq + 1).trim();
                switch (key) {
                    case "island selected" -> islandSelected = value;
                    case "island x offset" -> offsetX = parse(value, offsetX);
                    case "island y offset" -> offsetY = parse(value, offsetY);
                    case "island z offset" -> offsetZ = parse(value, offsetZ);
                    default -> LOGGER.warn("Unknown key in Skyblock.properties: {}", key);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read sky_block config", e);
        }
    }

    // copies the bundled island into config/sky_block/islands/ when that folder has no .nbt files
    private static void copyDefaultIsland() throws IOException {
        boolean hasIsland;
        try (var files = Files.list(islandsDir())) {
            hasIsland = files.anyMatch(p -> p.getFileName().toString().endsWith(".nbt"));
        }
        if (hasIsland) return;
        try (InputStream in = SkyConfig.class.getResourceAsStream("/sky_block_defaults/default_island.nbt")) {
            if (in == null) {
                LOGGER.warn("Bundled default island is missing from the mod jar");
                return;
            }
            Files.copy(in, islandsDir().resolve("Skyblock Island.nbt"));
        }
    }

    private static int parse(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return fallback; }
    }
}
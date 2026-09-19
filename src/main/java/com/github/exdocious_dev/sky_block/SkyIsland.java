package com.github.exdocious_dev.sky_block;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkyIsland {
    private static final Logger LOGGER = LoggerFactory.getLogger("sky_block");
    private static final int BASE_Y = 64;

    public static void init() {
        SkyConfig.load();
        ServerLifecycleEvents.SERVER_STARTED.register(SkyIsland::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        ServerLevel level = server.overworld();
        if (!SkyKeys.isSkyblock(level.getChunkSource().getGenerator())) return;

        Path marker = server.getWorldPath(LevelResource.ROOT).resolve("sky_block_island_placed");
        if (Files.exists(marker)) return;

        BlockPos corner = new BlockPos(-SkyConfig.offsetX, BASE_Y - SkyConfig.offsetY, -SkyConfig.offsetZ);
        boolean placed = placeIsland(server, level, corner);
        if (!placed) placeFallback(level);

        var source = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, "gamerule respawn_radius 0");
        server.getCommands().performPrefixedCommand(source, "setworldspawn 0 " + BASE_Y + " 0");

        if (placed) {
            try { Files.createFile(marker); } catch (IOException e) { LOGGER.error("Could not write island marker", e); }
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean placeIsland(MinecraftServer server, ServerLevel level, BlockPos corner) {
        Path file = SkyConfig.islandsDir().resolve(SkyConfig.islandSelected + ".nbt");
        LOGGER.info("Placing island {} with corner {}", file, corner);
        if (!Files.isRegularFile(file)) {
            LOGGER.warn("Island file not found: {}", file);
            return false;
        }
        try {
            Map<String, Object> root = (Map<String, Object>) readNbt(file);
            List<Object> size = (List<Object>) root.get("size");
            List<Object> palette = (List<Object>) root.get("palette");
            List<Object> blocks = (List<Object>) root.get("blocks");
            int sizeX = (Integer) size.get(0);
            int sizeZ = (Integer) size.get(2);

            // palette entry -> block state text, e.g. minecraft:oak_log[axis=y]
            String[] states = new String[palette.size()];
            for (int i = 0; i < states.length; i++) {
                Map<String, Object> entry = (Map<String, Object>) palette.get(i);
                String name = (String) entry.getOrDefault("id", entry.get("Name"));
                Map<String, Object> props = (Map<String, Object>) entry.getOrDefault("properties", entry.get("Properties"));
                if (name == null) throw new IOException("Palette entry has no block name: " + entry);
                StringBuilder sb = new StringBuilder(name);
                if (props != null && !props.isEmpty()) {
                    sb.append('[');
                    boolean first = true;
                    for (Map.Entry<String, Object> p : props.entrySet()) {
                        if (!first) sb.append(',');
                        first = false;
                        sb.append(p.getKey()).append('=').append(p.getValue());
                    }
                    sb.append(']');
                }
                states[i] = sb.toString();
            }

            // make sure every chunk the island touches exists first
            for (int cx = corner.getX() >> 4; cx <= (corner.getX() + sizeX) >> 4; cx++) {
                for (int cz = corner.getZ() >> 4; cz <= (corner.getZ() + sizeZ) >> 4; cz++) {
                    level.getChunk(cx, cz);
                }
            }

            CommandSourceStack loud = server.createCommandSourceStack();
            CommandSourceStack quiet = loud.withSuppressedOutput();
            int placed = 0;
            BlockPos firstPos = null;
            for (Object o : blocks) {
                Map<String, Object> block = (Map<String, Object>) o;
                String state = states[(Integer) block.get("state")];
                if (state.equals("minecraft:air") || state.equals("minecraft:cave_air")
                        || state.equals("minecraft:void_air") || state.equals("minecraft:structure_void")) continue;
                List<Object> p = (List<Object>) block.get("pos");
                BlockPos pos = corner.offset((Integer) p.get(0), (Integer) p.get(1), (Integer) p.get(2));
                String command = "setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + state;
                if (placed == 0) {
                    firstPos = pos;
                    LOGGER.info("First command: {}", command);
                }
                server.getCommands().performPrefixedCommand(placed == 0 ? loud : quiet, command);
                placed++;
            }
            if (firstPos == null) {
                LOGGER.warn("Island file has no solid blocks");
                return false;
            }
            boolean ok = !level.getBlockState(firstPos).isAir();
            LOGGER.info("Placed {} blocks, first block at {} is now {}", placed, firstPos, level.getBlockState(firstPos));
            return ok;
        } catch (Exception e) {
            LOGGER.error("Failed to place island {}", file, e);
            return false;
        }
    }

    private static void placeFallback(ServerLevel level) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(new BlockPos(x, BASE_Y - 2, z), Blocks.DIRT.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, BASE_Y - 1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    // ---- tiny NBT reader (no Minecraft classes involved) ----
    private static Object readNbt(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(file))))) {
            byte type = in.readByte();
            in.readUTF(); // root name
            return readPayload(in, type);
        }
    }

    private static Object readPayload(DataInputStream in, byte type) throws IOException {
        switch (type) {
            case 1: return in.readByte();
            case 2: return in.readShort();
            case 3: return in.readInt();
            case 4: return in.readLong();
            case 5: return in.readFloat();
            case 6: return in.readDouble();
            case 7: { byte[] a = new byte[in.readInt()]; in.readFully(a); return a; }
            case 8: return in.readUTF();
            case 9: {
                byte elementType = in.readByte();
                int n = in.readInt();
                List<Object> list = new ArrayList<>(n);
                for (int i = 0; i < n; i++) list.add(readPayload(in, elementType));
                return list;
            }
            case 10: {
                Map<String, Object> map = new HashMap<>();
                while (true) {
                    byte t = in.readByte();
                    if (t == 0) return map;
                    String key = in.readUTF();
                    map.put(key, readPayload(in, t));
                }
            }
            case 11: { int[] a = new int[in.readInt()]; for (int i = 0; i < a.length; i++) a[i] = in.readInt(); return a; }
            case 12: { long[] a = new long[in.readInt()]; for (int i = 0; i < a.length; i++) a[i] = in.readLong(); return a; }
            default: throw new IOException("Unknown NBT tag type " + type);
        }
    }
}
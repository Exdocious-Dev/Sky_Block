package com.github.exdocious_dev.sky_block;

import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SkyFilter {
    private static final Set<Block> ALLOWED_BLOCKS = Set.of(Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL);

    // structure id -> template pieces that generate fully (blocks and entities)
    private static final Map<Identifier, Set<String>> ALLOWED_PIECES = Map.of();

    // structure id -> template pieces where ONLY the elytra item frame is kept (becomes the armor stand)
    private static final Map<Identifier, Set<String>> ELYTRA_PIECES = Map.of(
            Identifier.withDefaultNamespace("end_city"), Set.of("ship"));

    private static final ThreadLocal<Identifier> STRUCTURE = new ThreadLocal<>();
    private static final ThreadLocal<int[]> ALLOW_DEPTH = ThreadLocal.withInitial(() -> new int[1]);
    private static final ThreadLocal<int[]> ELYTRA_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    public static void enterStructure(Identifier id) { STRUCTURE.set(id); }
    public static void exitStructure() { STRUCTURE.remove(); }

    public static void pushAllow() { ALLOW_DEPTH.get()[0]++; }
    public static void popAllow() { ALLOW_DEPTH.get()[0]--; }
    public static void pushElytra() { ELYTRA_DEPTH.get()[0]++; }
    public static void popElytra() { ELYTRA_DEPTH.get()[0]--; }

    public static boolean isPieceAllowed(String templateName) {
        Identifier s = STRUCTURE.get();
        return s != null && ALLOWED_PIECES.getOrDefault(s, Set.of()).contains(templateName);
    }

    public static boolean isElytraPiece(String templateName) {
        Identifier s = STRUCTURE.get();
        return s != null && ELYTRA_PIECES.getOrDefault(s, Set.of()).contains(templateName);
    }

    public static boolean allowBlock(BlockState state) {
        if (ALLOW_DEPTH.get()[0] > 0) return true;
        return STRUCTURE.get() != null && ALLOWED_BLOCKS.contains(state.getBlock());
    }

    public static boolean allowEntity() { return ALLOW_DEPTH.get()[0] > 0; }
    public static boolean allowElytraFrame() { return ELYTRA_DEPTH.get()[0] > 0; }
}
package com.github.exdocious_dev.sky_block;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class SkyKeys {
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = key("skyblock_overworld");
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = key("skyblock_nether");
    public static final ResourceKey<NoiseGeneratorSettings> END = key("skyblock_end");

    private static ResourceKey<NoiseGeneratorSettings> key(String path) {
        return ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("sky_block", path));
    }

    public static boolean isSkyblock(Holder<NoiseGeneratorSettings> s) {
        return s.is(OVERWORLD) || s.is(NETHER) || s.is(END);
    }

    public static boolean isSkyblock(ChunkGenerator g) {
        return g instanceof NoiseBasedChunkGenerator n && isSkyblock(n.generatorSettings());
    }
}
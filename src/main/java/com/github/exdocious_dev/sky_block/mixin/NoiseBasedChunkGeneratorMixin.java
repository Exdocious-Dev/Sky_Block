package com.github.exdocious_dev.sky_block.mixin;

import com.github.exdocious_dev.sky_block.SkyKeys;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    @Shadow public abstract Holder<NoiseGeneratorSettings> generatorSettings();

    // terrain fill + surface (icebergs etc.) + carvers (cave water/lava) all live in here
    @Inject(method = "buildTerrain", at = @At("HEAD"), cancellable = true)
    private void sky_block$noTerrain(ChunkAccess chunk, Blender blender, RandomState randomState,
                                     StructureManager structureManager, BiomeManager biomeManager,
                                     WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes,
                                     CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if (SkyKeys.isSkyblock(this.generatorSettings())) {
            cir.setReturnValue(CompletableFuture.completedFuture(chunk));
        }
    }

    @Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
    private void sky_block$noMobs(WorldGenRegion worldGenRegion, CallbackInfo ci) {
        if (SkyKeys.isSkyblock(this.generatorSettings())) ci.cancel();
    }

    @ModifyReturnValue(method = "stable", at = @At("RETURN"))
    private boolean sky_block$stable(boolean original, ResourceKey<NoiseGeneratorSettings> expectedPreset) {
        Holder<NoiseGeneratorSettings> s = this.generatorSettings();
        return original
                || (expectedPreset.equals(NoiseGeneratorSettings.OVERWORLD) && s.is(SkyKeys.OVERWORLD))
                || (expectedPreset.equals(NoiseGeneratorSettings.NETHER) && s.is(SkyKeys.NETHER))
                || (expectedPreset.equals(NoiseGeneratorSettings.END) && s.is(SkyKeys.END));
    }
}
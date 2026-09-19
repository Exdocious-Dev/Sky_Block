package com.github.exdocious_dev.sky_block.mixin;

import com.github.exdocious_dev.sky_block.SkyFilter;
import com.github.exdocious_dev.sky_block.SkyKeys;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndSpikeFeature.class)
public abstract class EndSpikeFeatureMixin {
    @WrapMethod(method = "place")
    private boolean sky_block$allowSpikes(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random,
                                          BlockPos origin, Operation<Boolean> original) {
        SkyFilter.pushAllow();
        if (!SkyKeys.isSkyblock(chunkGenerator)) return original.call(level, chunkGenerator, random, origin);
        try {
            return original.call(level, chunkGenerator, random, origin);
        } finally {
            SkyFilter.popAllow();
        }
    }
}
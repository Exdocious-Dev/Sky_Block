package com.github.exdocious_dev.sky_block.mixin;

import com.github.exdocious_dev.sky_block.SkyFilter;
import com.github.exdocious_dev.sky_block.SkyKeys;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    @Shadow public abstract Structure getStructure();

    @WrapMethod(method = "placeInChunk")
    private void sky_block$scope(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                                 RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, Operation<Void> original) {
        if (!SkyKeys.isSkyblock(generator)) { original.call(level, structureManager, generator, random, chunkBB, chunkPos); return; }
        Identifier id = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(this.getStructure());
        SkyFilter.enterStructure(id);
        try {
            original.call(level, structureManager, generator, random, chunkBB, chunkPos);
        } finally {
            SkyFilter.exitStructure();
        }
    }
}
package com.github.exdocious_dev.sky_block.mixin;

import com.github.exdocious_dev.sky_block.SkyFilter;
import com.github.exdocious_dev.sky_block.SkyKeys;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TemplateStructurePiece.class)
public abstract class TemplateStructurePieceMixin {
    @Shadow protected String templateName;

    @WrapMethod(method = "postProcess")
    private void sky_block$piece(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                                 RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pos,
                                 Operation<Void> original) {
        boolean skyblock = SkyKeys.isSkyblock(generator);
        boolean full = skyblock && SkyFilter.isPieceAllowed(this.templateName);
        boolean elytra = skyblock && SkyFilter.isElytraPiece(this.templateName);
        if (!full && !elytra) {
            original.call(level, structureManager, generator, random, chunkBB, chunkPos, pos);
            return;
        }
        if (full) SkyFilter.pushAllow();
        if (elytra) SkyFilter.pushElytra();
        try {
            original.call(level, structureManager, generator, random, chunkBB, chunkPos, pos);
        } finally {
            if (elytra) SkyFilter.popElytra();
            if (full) SkyFilter.popAllow();
        }
    }
}
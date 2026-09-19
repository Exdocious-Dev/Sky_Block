package com.github.exdocious_dev.sky_block.mixin;

import com.github.exdocious_dev.sky_block.SkyKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {
    @Shadow private ServerLevel level;
    @Shadow private BlockPos origin;
    @Shadow private BlockPos exitPortalLocation;

    @Inject(method = "spawnExitPortal", at = @At("HEAD"))
    private void sky_block$podiumY(boolean activated, CallbackInfo ci) {
        if (this.exitPortalLocation == null && SkyKeys.isSkyblock(this.level.getChunkSource().getGenerator())) {
            this.exitPortalLocation = EnderDragonFight.getPodiumLocation(this.origin).atY(64);
        }
    }
}
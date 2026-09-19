package com.github.exdocious_dev.sky_block.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStand.class)
public interface ArmorStandInvoker {
    @Invoker("setSmall") void sky_block$setSmall(boolean value);
    @Invoker("setShowArms") void sky_block$setShowArms(boolean value);
    @Invoker("setNoBasePlate") void sky_block$setNoBasePlate(boolean value);
}
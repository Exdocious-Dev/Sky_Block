package com.github.exdocious_dev.sky_block.mixin;

import com.github.exdocious_dev.sky_block.SkyFilter;
import com.github.exdocious_dev.sky_block.SkyKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    @Unique private Boolean sky_block$active;

    @Unique
    private boolean sky_block$active() {
        if (sky_block$active == null) {
            ServerLevel level = ((WorldGenRegion) (Object) this).getLevel();
            sky_block$active = SkyKeys.isSkyblock(level.getChunkSource().getGenerator());
        }
        return sky_block$active;
    }

    @Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
    private void sky_block$filterBlocks(BlockPos pos, BlockState state, int flags, int recursionLeft,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (sky_block$active() && !SkyFilter.allowBlock(state)) cir.setReturnValue(false);
    }

    @Unique
    private static ItemStack sky_block$stack(HolderLookup.Provider registries, String snbt) {
        try {
            CompoundTag tag = TagParser.parseCompoundFully(snbt);
            return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void sky_block$filterEntities(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!sky_block$active()) return;

        if (entity instanceof ItemFrame frame && frame.getItem().is(Items.ELYTRA)
                && (SkyFilter.allowEntity() || SkyFilter.allowElytraFrame())) {
            WorldGenRegion self = (WorldGenRegion) (Object) this;
            ArmorStand stand = EntityTypes.ARMOR_STAND.create(self.getLevel(), EntitySpawnReason.STRUCTURE);
            if (stand == null) {
                cir.setReturnValue(false);
                return;
            }
            BlockPos p = frame.blockPosition();
            stand.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, frame.getYRot(), 0.0F);

            ArmorStandInvoker invoker = (ArmorStandInvoker) stand;
            invoker.sky_block$setSmall(true);
            invoker.sky_block$setShowArms(true);
            invoker.sky_block$setNoBasePlate(true);
            stand.setNoGravity(true);

            HolderLookup.Provider registries = self.getLevel().registryAccess();
            stand.setItemSlot(EquipmentSlot.HEAD, sky_block$stack(registries,
                    "{id:\"minecraft:leather_helmet\",count:1,components:{\"minecraft:dyed_color\":9403439}}"));
            stand.setItemSlot(EquipmentSlot.CHEST, sky_block$stack(registries,
                    "{id:\"minecraft:elytra\",count:1}"));
            stand.setItemSlot(EquipmentSlot.LEGS, sky_block$stack(registries,
                    "{id:\"minecraft:leather_leggings\",count:1,components:{\"minecraft:dyed_color\":6825251}}"));
            stand.setItemSlot(EquipmentSlot.FEET, sky_block$stack(registries,
                    "{id:\"minecraft:leather_boots\",count:1,components:{\"minecraft:dyed_color\":6437809}}"));
            stand.setItemSlot(EquipmentSlot.MAINHAND, sky_block$stack(registries,
                    "{id:\"minecraft:diamond\",count:1}"));
            stand.setItemSlot(EquipmentSlot.OFFHAND, sky_block$stack(registries,
                    "{id:\"minecraft:enchanted_book\",count:1,components:{\"minecraft:stored_enchantments\":{\"minecraft:mending\":1}}}"));

            // the stand itself must pass the filter when it re-enters this method
            SkyFilter.pushAllow();
            try {
                cir.setReturnValue(self.addFreshEntity(stand));
            } finally {
                SkyFilter.popAllow();
            }
            return;
        }

        if (!SkyFilter.allowEntity()) cir.setReturnValue(false);
    }
}
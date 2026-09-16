package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 浮霜靴子：所有方块摩擦恒为 0.6（冰面不打滑）。
 * 目标为 NeoForge IBlockStateExtension#getFriction(LevelReader, BlockPos, Entity)。
 */
@Mixin(IBlockStateExtension.class)
public interface MixinBlockFriction {

    @Inject(
            method = "getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ddj$frostBootsFriction(LevelReader level, BlockPos pos, @Nullable Entity entity, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.is(ModItems.FROST_METAL_BOOTS.get())) {
                cir.setReturnValue(0.6F);
            }
        }
    }
}

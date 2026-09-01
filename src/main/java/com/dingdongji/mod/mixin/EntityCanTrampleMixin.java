package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 超限合金靴子：踩耕地不会踩坏（防践踏）。
 *
 * <p>NeoForge 把耕地践踏判定集中在 {@code Entity.canTrample(BlockState, BlockPos, float)}，
 * 只有返回 true 才会把耕地践踏成泥土。穿超限靴子的玩家强制返回 false，其余实体照常。</p>
 *
 * <p>注意：canTrample 是 NeoForge 注入到 Entity 的方法（不在原版 Mojang mappings），
 * 因此 @Inject 必须 remap=false，避免方法名被错误映射。</p>
 */
@Mixin(Entity.class)
public abstract class EntityCanTrampleMixin {

    @Inject(method = "canTrample", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$preventTrample(BlockState state, BlockPos pos, float fallDistance, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.is(ModItems.TRANSCENDIUM_BOOTS.get())) {
                cir.setReturnValue(false);
            }
        }
    }
}

package com.dingdongji.mod.mixin;

import com.dingdongji.mod.event.ModArmorSetHandler;
import com.dingdongji.mod.item.ModItems;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 炽足兽式液面站立：超限靴（水+岩浆）常驻；余烬靴（仅岩浆）受蹈火开关控制。
 * 潜行当 tick 取消托举。
 */
@Mixin(LivingEntity.class)
public abstract class MixinCanStandOnFluid {

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void ddj$canStandOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) return;
        if (player.isShiftKeyDown()) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.is(ModItems.TRANSCENDIUM_BOOTS.get())) {
            if (state.is(FluidTags.WATER) || state.is(FluidTags.LAVA)) {
                cir.setReturnValue(true);
            }
        } else if (boots.is(ModItems.EMBER_METAL_BOOTS.get()) && ModArmorSetHandler.isLavaWalkerEnabled(player)) {
            if (state.is(FluidTags.LAVA)) {
                cir.setReturnValue(true);
            }
        }
    }
}

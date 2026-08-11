package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 超限合金胸甲：受伤时取消屏幕抖动（不取消伤害，仅取消受伤动画）。
 * 拦截 Entity.animateHurt(float)，穿着超限胸甲时直接 cancel，
 * 使客户端不设置 hurtTime/hurtDuration，从而不显示受伤红屏抖动。
 */
@Mixin(Entity.class)
public abstract class MixinLivingEntityHurt {

    @Inject(method = "animateHurt", at = @At("HEAD"), cancellable = true)
    private void ddj$cancelHurtAnimation(float yaw, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.is(ModItems.TRANSCENDIUM_CHESTPLATE.get())) {
                ci.cancel();
            }
        }
    }
}

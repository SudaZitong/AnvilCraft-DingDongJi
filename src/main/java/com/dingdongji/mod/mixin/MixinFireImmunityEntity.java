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
 * 超限合金头盔：屏蔽 in_fire 状态（着火动画/火焰视觉效果）。
 * 拦截 setRemainingFireTicks，对超限头盔玩家始终设为 0。
 */
@Mixin(Entity.class)
public abstract class MixinFireImmunityEntity {

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
    private void ddj$blockFireTicks(int fireTicks, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(ModItems.TRANSCENDIUM_HELMET.get())) {
                ci.cancel();
            }
        }
    }
}

package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 超限合金头盔：岩浆明视效果（强烈穿透）。
 * 采用与知名模组 LavaClearView 相同的方案：
 * Redirect FogRenderer.setupFog 中对 Entity.isSpectator() 的调用，
 * 当玩家穿着超限头盔（且有抗火）时返回 true，使 setupFog 走"旁观者"分支，
 * 在岩浆中获得接近旁观者的清晰视野（远大于普通玩家的雾）。
 */
@Mixin(FogRenderer.class)
public abstract class MixinLavaVision {

    @Redirect(
            method = "setupFog",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z", ordinal = 0)
    )
    private static boolean ddj$lavaVision(Entity entity) {
        if (entity instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(ModItems.TRANSCENDIUM_HELMET.get())
                    && (player.isCreative() || player.isSpectator()
                        || player.hasEffect(MobEffects.FIRE_RESISTANCE))) {
                return true; // 强制走旁观者分支，岩浆视野清晰
            }
        }
        return entity.isSpectator();
    }
}

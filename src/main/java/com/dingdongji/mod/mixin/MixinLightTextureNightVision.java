package com.dingdongji.mod.mixin;

import com.dingdongji.mod.client.ClientArmorChecks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 超限头盔夜视模式：光照图路径视为满夜视，不依赖 NIGHT_VISION 药水。
 * LightTexture 里 player 编译期类型是 LocalPlayer，INVOKE 所有者必须对上。
 */
@Mixin(LightTexture.class)
public abstract class MixinLightTextureNightVision {

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z",
                    ordinal = 0
            )
    )
    private boolean ddj$helmetNightVision(LocalPlayer player, Holder<MobEffect> effect) {
        if (ClientArmorChecks.helmetNightVision()) {
            return true;
        }
        return player.hasEffect(effect);
    }
}

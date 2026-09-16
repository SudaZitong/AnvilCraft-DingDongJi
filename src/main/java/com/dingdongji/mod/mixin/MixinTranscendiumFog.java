package com.dingdongji.mod.mixin;

import com.dingdongji.mod.client.ClientArmorChecks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 超限头盔：全部雾距离/雾色按无雾处理（水、岩浆、细雪、虚空、失明/黑暗等）。
 */
@Mixin(FogRenderer.class)
public abstract class MixinTranscendiumFog {

    @Redirect(
            method = {"setupFog", "setupColor"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;")
    )
    private static FogType ddj$noFluidFog(Camera camera) {
        if (ClientArmorChecks.hasTranscendiumHelmet()) {
            return FogType.NONE;
        }
        return camera.getFluidInCamera();
    }

    @Inject(method = "setupFog", at = @At("RETURN"))
    private static void ddj$clearAllFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance,
                                        boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        if (ClientArmorChecks.hasTranscendiumHelmet()) {
            RenderSystem.setShaderFogStart(-8.0F);
            RenderSystem.setShaderFogEnd(1_000_000.0F);
        }
    }
}

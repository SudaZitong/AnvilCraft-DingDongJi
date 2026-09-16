package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 余烬头盔：取消着火 overlay。
 * 超限头盔：取消着火、水下扭曲、卡墙 overlay。
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class MixinFireOverlay {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void ddj$suppressFireOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.is(ModItems.EMBER_METAL_HELMET.get())
                || helmet.is(ModItems.TRANSCENDIUM_HELMET.get())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void ddj$suppressWaterOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TRANSCENDIUM_HELMET.get())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void ddj$suppressInWallOverlay(net.minecraft.client.renderer.texture.TextureAtlasSprite sprite,
                                                  PoseStack poseStack, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TRANSCENDIUM_HELMET.get())) {
            ci.cancel();
        }
    }
}

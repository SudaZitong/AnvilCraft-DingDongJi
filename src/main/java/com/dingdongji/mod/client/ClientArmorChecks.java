package com.dingdongji.mod.client;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 客户端装备判定，供雾/遮挡/夜视 Mixin 复用。
 */
public final class ClientArmorChecks {
    private ClientArmorChecks() {}

    public static Player localPlayer() {
        return Minecraft.getInstance().player;
    }

    public static boolean hasTranscendiumHelmet() {
        Player player = localPlayer();
        if (player == null) return false;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ModItems.TRANSCENDIUM_HELMET.get());
    }

    public static boolean hasEmberHelmet() {
        Player player = localPlayer();
        if (player == null) return false;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ModItems.EMBER_METAL_HELMET.get());
    }

    public static boolean hasFrostHelmet() {
        Player player = localPlayer();
        if (player == null) return false;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ModItems.FROST_METAL_HELMET.get());
    }

    public static boolean helmetNightVision() {
        return hasTranscendiumHelmet() && ClientAbilityState.nightVision();
    }
}

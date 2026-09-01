package com.dingdongji.mod.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 超限合金靴子 — 可在细雪上行走（类似浮霜靴子 / 原版皮革靴子）。
 *
 * <p>通过覆写 {@code canWalkOnPowderedSnow} 走原版细雪行走机制，
 * 无需每 tick 手动顶位置。</p>
 */
public class TranscendiumBootsItem extends ArmorItem {

    public TranscendiumBootsItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }
}

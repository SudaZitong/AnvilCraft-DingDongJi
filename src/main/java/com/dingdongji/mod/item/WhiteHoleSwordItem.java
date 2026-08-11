package com.dingdongji.mod.item;

import com.dingdongji.mod.item.component.AccumulateData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.Unbreakable;

public class WhiteHoleSwordItem extends SwordItem {
    public WhiteHoleSwordItem(int attackDamage) {
        super(Tiers.WOOD, new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant()
                .attributes(SwordItem.createAttributes(Tiers.WOOD, attackDamage, -2.4f))
                .component(ModComponents.ACCUMULATE.get(), AccumulateData.DEFAULT)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
        );
    }

    // 1243 天对应的 tick 数（接近 int 上限），超过后重置避免溢出
    private static final int MAX_TICKS = 2_147_000_000;

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !isSelected && slotId != 40 && entity instanceof Player) {
            AccumulateData data = stack.get(ModComponents.ACCUMULATE.get());
            if (data != null) {
                int ticks = data.ticks();
                // 达到上限后停止积蓄，等待玩家攻击消耗（减半后低于上限）再继续
                if (ticks < MAX_TICKS) {
                    stack.set(ModComponents.ACCUMULATE.get(), new AccumulateData(ticks + 1));
                }
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        AccumulateData data = stack.get(ModComponents.ACCUMULATE.get());
        if (data != null && data.ticks() > 0) {
            int totalTicks = data.ticks();
            stack.set(ModComponents.ACCUMULATE.get(), new AccumulateData(totalTicks / 2));

            // 全额积蓄伤害：每秒积蓄0.2点（每 tick 0.01），直接扣除生命值以绕过 boss 限伤逻辑
            float bonusDamage = totalTicks * 0.01f;
            if (bonusDamage > 0 && target.isAlive()) {
                float newHealth = target.getHealth() - bonusDamage;
                target.setHealth(Math.max(newHealth, 0.0f));
                if (newHealth <= 0.0f) {
                    // 触发死亡判定
                    target.hurt(target.damageSources().genericKill(), 0.0f);
                }
            }
        }
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 22;
    }
}

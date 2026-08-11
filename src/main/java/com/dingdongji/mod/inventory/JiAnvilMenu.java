package com.dingdongji.mod.inventory;

import net.minecraft.world.inventory.DataSlot;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.CommonHooks;

import com.dingdongji.mod.ModMenuTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JiAnvilMenu extends AnvilMenu {

    // 翻倍概率：10%
    private static final float DOUBLE_CHANCE = 0.10f;

    // ===== 翻倍结果缓存（只存 boolean，不存 ItemStack）=====
    // 每次 createResult 输出结果后清除，确保同物品下次再放可重新 roll
    private static final Map<String, Boolean> DOUBLE_RESULT_CACHE = new HashMap<>();
    private static final Map<String, Boolean> CURSE_RESULT_CACHE = new HashMap<>();

    private static String curseCacheKey(UUID uuid, ItemStack left, ItemStack right) {
        return uuid.toString() + "|curse|" + left.getItem().hashCode() + "|" + right.getItem().hashCode() + "|" + left.getComponents().hashCode() + "|" + right.getComponents().hashCode();
    }

    private static String doubleCacheKey(UUID uuid, ItemStack left, ItemStack right, Holder<Enchantment> holder, int level) {
        String enchId = holder.unwrapKey().map(k -> k.location().toString()).orElse("unknown");
        return uuid.toString() + "|"
            + left.getItem().hashCode() + "|"
            + right.getItem().hashCode() + "|"
            + left.getComponents().hashCode() + "|"
            + right.getComponents().hashCode() + "|"
            + enchId + "|"
            + level;
    }

    private static void clearDoubleCache(UUID uuid) {
        DOUBLE_RESULT_CACHE.entrySet().removeIf(e -> e.getKey().startsWith(uuid.toString() + "|"));
        CURSE_RESULT_CACHE.entrySet().removeIf(e -> e.getKey().startsWith(uuid.toString() + "|"));
    }

    // 反射获取 AnvilMenu 的私有字段
    private java.lang.reflect.Field costField;
    private java.lang.reflect.Field itemNameField;

    private DataSlot costData() {
        try {
            if (costField == null) {
                costField = AnvilMenu.class.getDeclaredField("cost");
                costField.setAccessible(true);
            }
            return (DataSlot) costField.get(this);
        } catch (Exception e) {
            return null;
        }
    }

    private String itemName() {
        try {
            if (itemNameField == null) {
                itemNameField = AnvilMenu.class.getDeclaredField("itemName");
                itemNameField.setAccessible(true);
            }
            return (String) itemNameField.get(this);
        } catch (Exception e) {
            return null;
        }
    }

    public JiAnvilMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory);
    }

    public JiAnvilMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.JI_ANVIL.get();
    }

    @Override
    public int getCost() {
        return super.getCost();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        // 取走结果后清除翻倍缓存，下次可重新 roll
        clearDoubleCache(player.getUUID());
    }

    @Override
    public void createResult() {
        ItemStack inputLeft = this.inputSlots.getItem(0);
        ItemStack inputRight = this.inputSlots.getItem(1);

        this.costData().set(1);
        int totalCost = 0;
        long repairCost = 0L;
        int repairCostT = 0;
        // 叽砧：允许所有非空物品进入附魔逻辑（包括有 Eternal/Unbreakable 的超限装备）
        if (!inputLeft.isEmpty()) {
            ItemStack inputLeftCopy = inputLeft.copy();
            final ItemEnchantments.Mutable enchantmentsOnLeft =
                new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(inputLeftCopy));
            repairCost += (long) inputLeft.getOrDefault(DataComponents.REPAIR_COST, 0)
                + (long) inputRight.getOrDefault(DataComponents.REPAIR_COST, 0);
            this.repairItemCountCost = 0;
            boolean hasStoredEnchantmentsOnInput2 = false;
            if (!CommonHooks.onAnvilChange(
                this, inputLeft, inputRight, this.resultSlots, this.itemName(), repairCost, this.player)) {
                return;
            }

            int damage;
            int repairItemCountCost;

            ChatFormatting extraFormat = null;
            if (inputRight.is(Items.NAME_TAG) && !inputLeft.isEmpty()) {
                if (!inputRight.has(DataComponents.CUSTOM_NAME)) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.costData().set(0);
                    return;
                }
                Component formattingText = inputRight.get(DataComponents.CUSTOM_NAME);
                if (formattingText == null) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.costData().set(0);
                    return;
                }
                String format = formattingText.getString();
                if (format.startsWith("&") && format.length() >= 2) {
                    extraFormat = ChatFormatting.getByCode(format.substring(1, 2).charAt(0));
                } else {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.costData().set(0);
                    return;
                }
            } else if (!inputRight.isEmpty()) {
                hasStoredEnchantmentsOnInput2 = inputRight.has(DataComponents.STORED_ENCHANTMENTS);
                int damageValue;
                if ((inputLeftCopy.isDamageableItem()
                    && inputLeftCopy.getItem().isValidRepairItem(inputLeft, inputRight))) {
                    damage = Math.min(inputLeftCopy.getDamageValue(), inputLeftCopy.getMaxDamage() / 4);
                    if (damage <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.costData().set(0);
                        return;
                    }
                    for (repairItemCountCost = 0;
                         damage > 0 && repairItemCountCost < inputRight.getCount();
                         ++repairItemCountCost) {
                        damageValue = inputLeftCopy.getDamageValue() - damage;
                        inputLeftCopy.setDamageValue(damageValue);
                        ++totalCost;
                        damage = Math.min(inputLeftCopy.getDamageValue(), inputLeftCopy.getMaxDamage() / 4);
                    }
                    this.repairItemCountCost = repairItemCountCost;
                } else {
                    if (!hasStoredEnchantmentsOnInput2
                        && (!inputLeftCopy.is(inputRight.getItem())
                        || !inputLeftCopy.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.costData().set(0);
                        return;
                    }

                    if (inputLeftCopy.isDamageableItem() && !hasStoredEnchantmentsOnInput2) {
                        damage = inputLeft.getMaxDamage() - inputLeft.getDamageValue();
                        repairItemCountCost = inputRight.getMaxDamage() - inputRight.getDamageValue();
                        damageValue = repairItemCountCost + inputLeftCopy.getMaxDamage() * 12 / 100;
                        int k1 = damage + damageValue;
                        int l1 = inputLeftCopy.getMaxDamage() - k1;
                        if (l1 < 0) l1 = 0;
                        if (l1 < inputLeftCopy.getDamageValue()) {
                            inputLeftCopy.setDamageValue(l1);
                            totalCost += 2;
                        }
                    }

                    // ===== 叽砧核心：附魔翻倍逻辑（缓存 boolean，不缓存 ItemStack）=====
                    // 附魔书使用 STORED_ENCHANTMENTS，普通附魔物品使用 ENCHANTMENTS
                    ItemEnchantments enchantmentsOnRight = hasStoredEnchantmentsOnInput2
                            ? inputRight.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                            : EnchantmentHelper.getEnchantmentsForCrafting(inputRight);
                    boolean flag2 = false;
                    boolean flag3 = false;

                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantmentsOnRight.entrySet()) {
                        Holder<Enchantment> holder = entry.getKey();
                        int leftLevel = enchantmentsOnLeft.getLevel(holder);
                        int rightLevel = entry.getIntValue();
                        Enchantment enchantment = holder.value();

                        // 兼容性检查
                        boolean flag1 = inputLeftCopy.supportsEnchantment(holder);
                        if (this.player.getAbilities().instabuild) {
                            flag1 = true;
                        }
                        for (Holder<Enchantment> holder1 : enchantmentsOnLeft.keySet()) {
                            if (!holder1.equals(holder) && !Enchantment.areCompatible(holder, holder1)) {
                                flag1 = false;
                                totalCost++;
                            }
                        }
                        if (!flag1) {
                            flag3 = true;
                            continue;
                        }
                        flag2 = true;

                        // 叽砧：始终允许突破附魔上限
                        int resultLevel;

                        if (leftLevel == rightLevel) {
                            // 同级合并：10%概率翻倍（leftLevel+rightLevel），否则+1
                            // 用缓存防单次事件内重复调用导致闪烁，结果输出后清除，下次可重新 roll
                            String cacheKey = doubleCacheKey(this.player.getUUID(), inputLeft, inputRight, holder, leftLevel);
                            Boolean cached = DOUBLE_RESULT_CACHE.get(cacheKey);
                            boolean doDouble;
                            if (cached != null) {
                                doDouble = cached;
                            } else {
                                doDouble = this.player.getRandom().nextFloat() < DOUBLE_CHANCE;
                                DOUBLE_RESULT_CACHE.put(cacheKey, doDouble);
                            }

                            if (doDouble) {
                                resultLevel = leftLevel + rightLevel;
                            } else {
                                resultLevel = leftLevel + 1;
                            }
                        } else {
                            // 不同级：取较大值
                            resultLevel = Math.max(rightLevel, leftLevel);
                        }

                        enchantmentsOnLeft.set(holder, resultLevel);
                        // 20%概率附加随机诅咒附魔（防刷：缓存结果，取走后才重置）
                        String curseKey = curseCacheKey(this.player.getUUID(), inputLeft, inputRight);
                        Boolean cachedCurse = CURSE_RESULT_CACHE.get(curseKey);
                        if (cachedCurse == null) {
                            cachedCurse = this.player.getRandom().nextFloat() < 0.20f;
                            CURSE_RESULT_CACHE.put(curseKey, cachedCurse);
                        }
                        if (cachedCurse) {
                            java.util.List<Holder<Enchantment>> curses = new java.util.ArrayList<>();
                            var enchRegistry = this.player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                            for (Holder<Enchantment> possible : enchRegistry.listElements().toList()) {
                                if (possible.is(net.minecraft.tags.EnchantmentTags.CURSE) && enchantmentsOnLeft.getLevel(possible) <= 0 && inputLeftCopy.supportsEnchantment(possible)) {
                                    curses.add(possible);
                                }
                            }
                            if (!curses.isEmpty()) {
                                Holder<Enchantment> curse = curses.get(this.player.getRandom().nextInt(curses.size()));
                                enchantmentsOnLeft.set(curse, 1);
                            }
                        }
    
                        int anvilCost = enchantment.getAnvilCost();
                        if (hasStoredEnchantmentsOnInput2) {
                            anvilCost = Math.max(1, anvilCost / 2);
                        }

                        // 叽砧费用计算：翻2倍（原生费用 anvilCost * rightLevel，再翻倍）
                        long enchantCost = (long) anvilCost * rightLevel * 2;
                        enchantCost = enchantCost * inputLeft.getCount() * inputLeft.getCount();
                        totalCost += (int) Math.min(enchantCost, Integer.MAX_VALUE);

                        if (inputLeft.getCount() > 1) {
                            totalCost = 99999999;
                        }
                    }

                    if (flag3 && !flag2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.costData().set(0);
                        return;
                    }
                }
            }

            if (extraFormat != null) {
                repairCostT = 1;
                totalCost += repairCostT * inputLeft.getCount() * inputRight.getCount();
                Component currentName = inputLeft.getHoverName();
                if (!this.itemName().equals(currentName.getString())
                    && this.itemName() != null
                    && !this.itemName().isBlank()) {
                    currentName = Component.literal(this.itemName());
                }
                inputLeftCopy.set(DataComponents.CUSTOM_NAME, currentName.copy().withStyle(extraFormat));
            } else {
                if (this.itemName() != null && !StringUtil.isBlank(this.itemName())) {
                    boolean nameChanged = !this.itemName().equals(inputLeft.getHoverName().getString());
                    if (nameChanged) {
                        repairCostT = 1;
                        totalCost += repairCostT;
                        inputLeftCopy.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName()));
                    }
                } else {
                    if (inputLeft.has(DataComponents.CUSTOM_NAME)) {
                        repairCostT = 1;
                        totalCost += repairCostT;
                        inputLeftCopy.remove(DataComponents.CUSTOM_NAME);
                    }
                }
            }

            if (hasStoredEnchantmentsOnInput2 && !inputLeftCopy.isBookEnchantable(inputRight)) {
                inputLeftCopy = ItemStack.EMPTY;
            }

            damage = (int) Mth.clamp(repairCost + (long) totalCost, 0L, 2147483647L);
            this.costData().set(damage); // 叽砧：真实消耗写入 DataSlot（用于实际扣经验）
            if (totalCost <= 0) {
                inputLeftCopy = ItemStack.EMPTY;
            }

            if (!inputLeftCopy.isEmpty()) {
                repairItemCountCost = inputLeftCopy.getOrDefault(DataComponents.REPAIR_COST, 0);
                if (repairItemCountCost < inputRight.getOrDefault(DataComponents.REPAIR_COST, 0)) {
                    repairItemCountCost = inputRight.getOrDefault(DataComponents.REPAIR_COST, 0);
                }
                if (repairCostT != totalCost || repairCostT == 0) {
                    repairItemCountCost = calculateIncreasedRepairCost(repairItemCountCost);
                }
                inputLeftCopy.set(DataComponents.REPAIR_COST, repairItemCountCost);
                EnchantmentHelper.setEnchantments(inputLeftCopy, enchantmentsOnLeft.toImmutable());
            }

            this.resultSlots.setItem(0, inputLeftCopy);
            this.broadcastChanges();
        } else {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.costData().set(0);
        }
    }
}

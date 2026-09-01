package com.dingdongji.mod.util;

import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 铁砧工艺运行时兼容工具。
 * 通过 RegistryAccess 查找铁砧工艺的 DataComponent，避免编译期依赖。
 * 用于 ModArmorSetHandler 中运行时补齐旧物品的组件。
 */
public class AnvilCraftCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation REFORGING_ID = ResourceLocation.parse("anvilcraft:reforging");
    private static final ResourceLocation ETERNAL_ID = ResourceLocation.parse("anvilcraft:eternal");
    private static final ResourceLocation PROVIDENCE_ID = ResourceLocation.parse("anvilcraft:providence");

    // 缓存
    @Nullable
    private static DataComponentType<?> cachedReforging = null;
    @Nullable
    private static DataComponentType<?> cachedEternal = null;
    @Nullable
    private static DataComponentType<?> cachedProvidence = null;
    private static boolean initialized = false;

    // 组件值缓存（反射获取单例）
    @Nullable
    private static Object eternalInstance = null;
    @Nullable
    private static Object providenceInstance = null;

    // 飘升机物品类缓存
    @Nullable
    private static Class<?> ionocraftBackpackItemClass = null;
    @Nullable
    private static java.lang.reflect.Method ionocraftGetByPlayer = null;
    @Nullable
    private static java.lang.reflect.Method ionocraftGetEnergy = null;
    private static boolean ionocraftChecked = false;

    /**
     * 初始化缓存：查找所有铁砧工艺组件并缓存
     */
    public static void init(RegistryAccess access) {
        if (initialized) return;
        initialized = true;

        Registry<DataComponentType<?>> registry = access.registryOrThrow(Registries.DATA_COMPONENT_TYPE);

        cachedReforging = registry.get(REFORGING_ID);
        cachedEternal = registry.get(ETERNAL_ID);
        cachedProvidence = registry.get(PROVIDENCE_ID);

        // 反射获取 Eternal.INSTANCE
        if (cachedEternal != null) {
            try {
                Class<?> eternalClass = Class.forName("dev.dubhe.anvilcraft.item.property.component.Eternal");
                java.lang.reflect.Field instanceField = eternalClass.getDeclaredField("INSTANCE");
                instanceField.setAccessible(true);
                eternalInstance = instanceField.get(null);
            } catch (Exception e) {
                LOGGER.warn("[DingDongJi] 无法反射获取 Eternal.INSTANCE", e);
            }
        }

        // 反射获取 Providence.INSTANCE
        if (cachedProvidence != null) {
            try {
                Class<?> providenceClass = Class.forName("dev.dubhe.anvilcraft.item.property.component.Providence");
                java.lang.reflect.Field instanceField = providenceClass.getDeclaredField("INSTANCE");
                instanceField.setAccessible(true);
                providenceInstance = instanceField.get(null);
            } catch (Exception e) {
                LOGGER.warn("[DingDongJi] 无法反射获取 Providence.INSTANCE", e);
            }
        }

        LOGGER.info("[DingDongJi] AnvilCraftCompat 初始化: reforging={}, eternal={}, providence={}",
                cachedReforging != null, cachedEternal != null, cachedProvidence != null);
    }

    public static boolean isLoaded() {
        return cachedReforging != null || cachedProvidence != null;
    }

    /** 赋予重铸组件（Unit 单例） */
    @SuppressWarnings("unchecked")
    public static void setReforging(ItemStack stack) {
        if (cachedReforging != null && !stack.has(cachedReforging)) {
            DataComponentType<Unit> type = (DataComponentType<Unit>) cachedReforging;
            stack.set(type, Unit.INSTANCE);
        }
    }

    /** 赋予永恒组件（Eternal.INSTANCE） */
    @SuppressWarnings("unchecked")
    public static void setEternal(ItemStack stack) {
        if (cachedEternal != null && eternalInstance != null && !stack.has(cachedEternal)) {
            DataComponentType<Object> type = (DataComponentType<Object>) cachedEternal;
            stack.set(type, eternalInstance);
        }
    }

    /** 赋予强运组件（Providence.INSTANCE） */
    @SuppressWarnings("unchecked")
    public static void setFortune(ItemStack stack) {
        if (cachedProvidence != null && providenceInstance != null && !stack.has(cachedProvidence)) {
            DataComponentType<Object> type = (DataComponentType<Object>) cachedProvidence;
            stack.set(type, providenceInstance);
        }
    }

    // ==================================================================
    //  飘升机（Ionocraft Backpack）检测
    // ==================================================================

    /**
     * 初始化飘升机反射缓存（延迟初始化，首次调用时执行）
     */
    private static void initIonocraft() {
        if (ionocraftChecked) return;
        ionocraftChecked = true;
        // 兼容铁砧工艺飘升机类名大小写变化：
        // 旧版(1.6.0及之前)为 IonoCraftBackpackItem，新版(snapshot.2142及之后)改为 IonocraftBackpackItem
        String[] candidates = {
                "dev.dubhe.anvilcraft.item.IonocraftBackpackItem",
                "dev.dubhe.anvilcraft.item.IonoCraftBackpackItem"
        };
        for (String clsName : candidates) {
            try {
                ionocraftBackpackItemClass = Class.forName(clsName);
                ionocraftGetByPlayer = ionocraftBackpackItemClass.getDeclaredMethod("getByPlayer", Player.class);
                ionocraftGetByPlayer.setAccessible(true);
                ionocraftGetEnergy = ionocraftBackpackItemClass.getDeclaredMethod("getEnergyStored", ItemStack.class);
                ionocraftGetEnergy.setAccessible(true);
                LOGGER.info("[DingDongJi] 飘升机反射初始化成功: " + clsName);
                return;
            } catch (Exception e) {
                ionocraftBackpackItemClass = null;
                ionocraftGetByPlayer = null;
                ionocraftGetEnergy = null;
                // 尝试下一个候选类名
            }
        }
        LOGGER.info("[DingDongJi] 飘升机未安装或反射失败，跳过");
    }

    /**
     * 检测玩家是否佩戴了有电量的飘升机（背包或饰品栏均可）
     * @return 飘升机 ItemStack（empty 表示没有）
     */
    public static ItemStack getIonocraftBackpack(Player player) {
        initIonocraft();
        if (ionocraftBackpackItemClass == null || ionocraftGetByPlayer == null) return ItemStack.EMPTY;
        try {
            Object result = ionocraftGetByPlayer.invoke(null, player);
            if (result instanceof ItemStack stack && !stack.isEmpty()) return stack;
        } catch (Exception e) {
            // 静默
        }
        return ItemStack.EMPTY;
    }

    /**
     * 检测玩家是否佩戴了有电量的飘升机
     */
    public static boolean hasActiveIonocraftBackpack(Player player) {
        ItemStack backpack = getIonocraftBackpack(player);
        if (backpack.isEmpty()) return false;
        if (ionocraftGetEnergy == null) return false;
        try {
            int energy = (int) ionocraftGetEnergy.invoke(null, backpack);
            return energy > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

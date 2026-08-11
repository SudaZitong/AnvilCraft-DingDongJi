package com.dingdongji.mod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.dingdongji.mod.item.ModItems;

/**
 * 超限合金套全套效果：免疫铁砧工艺锻星砧星体的引力牵引
 *
 * AnvilCraft 1.6 的 GravityManager 有两个 getGravityVector 重载：
 * - getGravityVector(Entity) → 被 anvilcraft$ApplyGravity 调用（修改 Entity.getGravity() 返回值）
 * - getGravityVector(Entity, double) → 被 anvilcraft$ApplyHorizontalGravity 调用（在 tick() TAIL 修改 deltaMovement）
 *
 * 两个都必须拦截，否则水平牵引不会被取消。
 *
 * getGravityVector 返回的是 AnvilCraft 额外引力（不包含原版重力），
 * 返回 Vec3.ZERO 不影响原版重力下落。
 */
@Mixin(targets = "dev.dubhe.anvilcraft.util.GravityManager")
public abstract class GravityManagerMixin {

    private static boolean hasFullTranscendiumSet(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TRANSCENDIUM_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TRANSCENDIUM_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.TRANSCENDIUM_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TRANSCENDIUM_BOOTS.get());
    }

    /**
     * 拦截 getGravityVector(Entity) — 单参数重载
     * 被 anvilcraft$ApplyGravity 调用，影响 Entity.getGravity() 返回值
     */
    @Inject(method = "getGravityVector(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dingdongji$cancelGravity1(Entity entity, CallbackInfoReturnable<Vec3> cir) {
        if (hasFullTranscendiumSet(entity)) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }

    /**
     * 拦截 getGravityVector(Entity, double) — 双参数重载
     * 被 anvilcraft$ApplyHorizontalGravity 调用，在 tick() TAIL 修改 deltaMovement
     * 这才是水平牵引的来源！
     */
    @Inject(method = "getGravityVector(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dingdongji$cancelGravity2(Entity entity, double movement, CallbackInfoReturnable<Vec3> cir) {
        if (hasFullTranscendiumSet(entity)) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}

package com.dingdongji.mod.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 超限全套：覆盖维度重力标量，使月球/虚空行星垂直重力恢复为原版 0.08。
 * priority 1500，保证在 anvilcraft$ApplyGravity 之后覆盖返回值。
 */
@Mixin(value = Entity.class, priority = 1500)
public abstract class MixinEntityGravity {

    @Inject(method = "getGravity()D", at = @At("RETURN"), cancellable = true)
    private void ddj$restoreGravity(CallbackInfoReturnable<Double> cir) {
        Entity self = (Entity) (Object) this;
        if (GravityManagerMixin.hasFullTranscendiumSet(self)) {
            cir.setReturnValue(0.08);
        }
    }
}

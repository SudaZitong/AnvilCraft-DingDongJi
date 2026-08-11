package com.dingdongji.mod.mixin;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 移除护甲值和盔甲韧性的上限，使浮霜金属套的无义转换可以无限攀升。
 * 原版上限：ARMOR=30, ARMOR_TOUGHNESS=20
 * 修改后：无上限
 */
@Mixin(RangedAttribute.class)
public class RangedAttributeMixin {

    /**
     * 修改 RangedAttribute 构造函数中 max 参数的值（第4个参数，index=3）。
     * 注意：@At("HEAD") 在 super() 之前，handler 必须为 static。
     */
    @ModifyVariable(
        method = "<init>(Ljava/lang/String;DDD)V",
        at = @At("HEAD"),
        argsOnly = true,
        index = 3
    )
    private static double removeMaxCap(double max, String descriptionId, double defaultValue, double min) {
        if ("attribute.name.generic.armor".equals(descriptionId) 
            || "attribute.name.generic.armor_toughness".equals(descriptionId)) {
            return Double.MAX_VALUE;
        }
        return max;
    }
}

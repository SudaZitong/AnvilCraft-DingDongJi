package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 浮霜锻造台 Mixin
 *
 * 让创造模板被视为可用模板，使其能出现在模板面板（配合展开逻辑显示 ε 嬗变 / ζ 形变）。
 * 其余由现有配方 mixin（DeformationRecipeMixin / PermutationRecipeMixin）处理匹配。
 */
@Mixin(targets = "dev.dubhe.anvilcraft.inventory.FrostSmithingMenu")
public abstract class FrostSmithingMenuMixin {

    @Inject(method = "isUsableTemplate", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$usableTemplate(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // 创造模板视为可用模板，使其出现在模板面板（显示 ε/ζ）
        if (ModItems.isCreateTemplate(stack)) {
            cir.setReturnValue(true);
        }
    }
}

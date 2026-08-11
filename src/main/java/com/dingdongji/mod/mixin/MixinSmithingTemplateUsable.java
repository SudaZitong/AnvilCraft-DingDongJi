package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 皇家钢 / 余烬 / 浮霜 锻造台：isUsableTemplate 原本只识别「能作为当前配方模板原料」的物品，
 * 而创造模板的非普通模式（β~ζ）不被 isTemplateIngredient 识别，导致容器里放其他模式的创造
 * 模板时，被 collectTemplates 过滤掉、锻造台借入/识别不了。
 *
 * <p>这里对「任意模式的创造模板」一律放行为可用模板：
 * <ul>
 *   <li>皇家钢：识别任意模式创造模板，借入后统一显示普通模式 α</li>
 *   <li>余烬 / 浮霜：识别任意模式创造模板，按其模式展开/借入</li>
 * </ul>
 * 非创造模板仍走原逻辑，其余功能保持不变。
 */
@Pseudo
@Mixin(targets = {
        "dev.dubhe.anvilcraft.inventory.RoyalSmithingMenu",
        "dev.dubhe.anvilcraft.inventory.EmberSmithingMenu",
        "dev.dubhe.anvilcraft.inventory.FrostSmithingMenu"
}, remap = false)
public abstract class MixinSmithingTemplateUsable {

    @Inject(method = "isUsableTemplate", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$usableTemplate(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ModItems.isCreateTemplate(stack)) {
            cir.setReturnValue(true);
        }
    }
}

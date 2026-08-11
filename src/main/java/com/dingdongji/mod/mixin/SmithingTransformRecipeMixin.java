package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 创造模板 Mixin — 支持模式切换
 *
 * - isTemplateIngredient: 仅 α（普通）模式允许进入模板槽
 * - template.test() @Redirect: 根据当前模式判断是否匹配配方
 */
@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingTransformRecipeMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("dingdongji");

    @Inject(method = "isTemplateIngredient", at = @At("HEAD"), cancellable = true)
    private void dingdongji$isTemplateIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ModItems.isCreateTemplate(stack)) {
            CreateTemplateMode mode = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
            if (mode == null) mode = CreateTemplateMode.DEFAULT;
            // 仅 α（普通）模式可放入普通锻造台，其他模式需要专用锻造台
            cir.setReturnValue(CreateTemplateMode.ALPHA.equals(mode));
        }
    }

    @Redirect(
        method = "matches",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z",
            ordinal = 0
        )
    )
    private boolean dingdongji$templateTest(Ingredient ingredient, ItemStack stack) {
        if (!ModItems.isCreateTemplate(stack)) {
            return ingredient.test(stack);
        }

        CreateTemplateMode mode = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
        if (mode == null) mode = CreateTemplateMode.DEFAULT;

        // α 模式：可替代所有模板
        if (CreateTemplateMode.ALPHA.equals(mode)) {
            return true;
        }

        // 其他模式：检查配方的模板材料是否与当前模式匹配
        String targetId = mode.getTargetTemplateId();
        if (targetId == null) return true;

        Item targetItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(targetId));
        if (targetItem == Items.AIR) return false; // 该模板不存在（非1.6）

        // 检查 ingredient 中是否包含目标物品
        for (ItemStack ingredientStack : ingredient.getItems()) {
            if (ingredientStack.is(targetItem)) {
                return true;
            }
        }
        return false;
    }
}

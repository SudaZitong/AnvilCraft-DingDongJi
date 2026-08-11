package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 皇家钢 / 余烬 / 浮霜 锻造台（都继承 anvilcraft 的 AdjacentSmithingMenu）：
 * 当创造模板放在锻造台旁的容器中时，在左侧模板面板把创造模板"展开"成对应模式的多个条目，
 * 一个模式占一个格子。按锻造台类型决定展开哪些模式：
 *   - 皇家钢（Royal）：只显示普通模式 α
 *   - 余烬（Ember）：只显示二合一 β、四合一 γ、八合一 δ（依次放入）
 *   - 浮霜（Frost）：只显示嬗变 ε、形变 ζ
 *   - 其他类型默认只显示 α
 *
 * anvilcraft 原版 collectTemplates 用 addUniqueTemplate 按"物品 ID"去重，
 * 而创造模板所有模式同为一个 ID，故原本只显示一个。这里在 refreshTemplateCatalog 结束后，
 * 把面板列表中的创造模板替换为其展开的模式条目，绕过 ID 去重。
 */
@Pseudo
@Mixin(targets = "dev.dubhe.anvilcraft.inventory.AdjacentSmithingMenu", remap = false)
public abstract class MixinAdjacentSmithingMenu {

    @Inject(method = "refreshTemplateCatalog", at = @At("TAIL"))
    private void dingdongji$expandCreateTemplate(CallbackInfo ci) {
        try {
            Class<?> clazz = Class.forName("dev.dubhe.anvilcraft.inventory.AdjacentSmithingMenu");
            Field field = clazz.getDeclaredField("adjacentTemplates");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<ItemStack> templates = (List<ItemStack>) field.get(this);
            if (templates == null || templates.isEmpty()) return;

            int createIdx = -1;
            for (int i = 0; i < templates.size(); i++) {
                if (ModItems.isCreateTemplate(templates.get(i))) {
                    createIdx = i;
                    break;
                }
            }
            if (createIdx < 0) return;

            List<CreateTemplateMode> modes = ddj$modesForTable((Object) this);
            if (modes.isEmpty()) return;

            ItemStack base = templates.get(createIdx).copyWithCount(1);
            List<ItemStack> expansion = new ArrayList<>(modes.size());
            for (CreateTemplateMode m : modes) {
                ItemStack st = base.copy();
                st.set(ModComponents.CREATE_TEMPLATE_MODE.get(), m);
                expansion.add(st);
            }

            // templates 是 new ArrayList（可变），可 remove/add
            templates.remove(createIdx);
            templates.addAll(0, expansion);

            Field dirtyField = clazz.getDeclaredField("templateDataDirty");
            dirtyField.setAccessible(true);
            dirtyField.setBoolean(this, true);
        } catch (Exception e) {
            // 静默忽略
        }
    }

    /** 根据当前锻造台类型返回要展开的模式列表。 */
    private static List<CreateTemplateMode> ddj$modesForTable(Object menu) {
        List<CreateTemplateMode> modes = new ArrayList<>();
        try {
            if (Class.forName("dev.dubhe.anvilcraft.inventory.RoyalSmithingMenu").isInstance(menu)) {
                modes.add(CreateTemplateMode.ALPHA);
            } else if (Class.forName("dev.dubhe.anvilcraft.inventory.EmberSmithingMenu").isInstance(menu)) {
                modes.add(CreateTemplateMode.BETA);
                modes.add(CreateTemplateMode.GAMMA);
                modes.add(CreateTemplateMode.DELTA);
            } else if (Class.forName("dev.dubhe.anvilcraft.inventory.FrostSmithingMenu").isInstance(menu)) {
                modes.add(CreateTemplateMode.EPSILON);
                modes.add(CreateTemplateMode.ZETA);
            } else {
                modes.add(CreateTemplateMode.ALPHA);
            }
        } catch (Exception e) {
            modes.add(CreateTemplateMode.ALPHA);
        }
        return modes;
    }

    /**
     * 创造模板按 mode 比较选中状态（原 isBorrowedTemplate 只比较 itemId，导致所有模式全亮选中）。
     * 当 borrowed 和当前 stack 都是创造模板时，比较 mode 组件；否则原逻辑保持不变。
     */
    @Inject(method = "isBorrowedTemplate", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$isBorrowedTemplate(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            java.lang.reflect.Field f = Class.forName("dev.dubhe.anvilcraft.inventory.AdjacentSmithingMenu")
                    .getDeclaredField("borrowedTemplateStack");
            f.setAccessible(true);
            ItemStack borrowed = (ItemStack) f.get(this);
            if (ModItems.isCreateTemplate(stack) && ModItems.isCreateTemplate(borrowed)) {
                CreateTemplateMode m1 = stack.getOrDefault(
                        ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
                CreateTemplateMode m2 = borrowed.getOrDefault(
                        ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
                cir.setReturnValue(m1.mode().equals(m2.mode()));
            }
        } catch (Exception e) {
            // 静默
        }
    }
}

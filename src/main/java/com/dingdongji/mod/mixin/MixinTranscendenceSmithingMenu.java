package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 超限锻造台 Mixin — 创造模板在模板列表中排在第一位
 *
 * TranscendenceSmithingMenu 不继承 AdjacentSmithingMenu，
 * 有自己的 refreshTemplateCatalog() 和 templates 字段。
 * 此 Mixin 在 refreshTemplateCatalog 尾部注入，将创造模板移到列表第一位。
 */
@Pseudo
@Mixin(targets = "dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu", remap = false)
public abstract class MixinTranscendenceSmithingMenu {

    @Unique
    private static final ResourceLocation CREATE_TEMPLATE_ID = ResourceLocation.parse("dingdongji:create_template");

    @Inject(method = "refreshTemplateCatalog", at = @At("TAIL"))
    private void dingdongji$prioritizeCreateTemplate(CallbackInfo ci) {
        try {
            Class<?> clazz = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu");
            java.lang.reflect.Field field = clazz.getDeclaredField("templates");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<ItemStack> templates = (List<ItemStack>) field.get(this);
            if (templates == null || templates.size() <= 1) return;

            int createIdx = -1;
            for (int i = 0; i < templates.size(); i++) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(templates.get(i).getItem());
                if (CREATE_TEMPLATE_ID.equals(id)) {
                    createIdx = i;
                    break;
                }
            }
            if (createIdx < 0) return;

            // templates 是 List.of()/toList() 生成的不可变列表，不能直接 remove。
            // 把创造模板展开成全部模式（α~ζ）放到列表最前面，其余模板依次排后。
            ItemStack base = templates.get(createIdx).copyWithCount(1);
            List<ItemStack> expansion = new ArrayList<>();
            expansion.add(base.copy()); // α 普通
            ItemStack beta = base.copy(); beta.set(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.BETA); expansion.add(beta);
            ItemStack gamma = base.copy(); gamma.set(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.GAMMA); expansion.add(gamma);
            ItemStack delta = base.copy(); delta.set(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DELTA); expansion.add(delta);
            ItemStack eps = base.copy(); eps.set(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.EPSILON); expansion.add(eps);
            ItemStack zeta = base.copy(); zeta.set(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.ZETA); expansion.add(zeta);

            List<ItemStack> reordered = new ArrayList<>(templates.size() + expansion.size());
            reordered.addAll(expansion);
            for (int i = 0; i < templates.size(); i++) {
                if (i != createIdx) reordered.add(templates.get(i));
            }
            field.set(this, reordered);

            java.lang.reflect.Field dirtyField = clazz.getDeclaredField("templateDataDirty");
            dirtyField.setAccessible(true);
            dirtyField.setBoolean(this, true);
        } catch (Exception e) {
            // 静默忽略
        }
    }

    /**
     * 让创造模板按模式决定超限的配方模式：
     *   α(普通) → ROYAL(皇家)、β/γ/δ → EMBER(余烬)、ε/ζ → FROST(浮霜)
     * 否则按原逻辑（selectedTemplate 类型）判定，返回皇家。
     */
    @Inject(method = "getMode", at = @At("HEAD"), cancellable = true, remap = false)
    private void dingdongji$createTemplateMode(CallbackInfoReturnable<Object> cir) {
        try {
            Field f = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu")
                    .getDeclaredField("selectedTemplate");
            f.setAccessible(true);
            ItemStack sel = (ItemStack) f.get(this);
            if (sel == null || !ModItems.isCreateTemplate(sel)) return;

            CreateTemplateMode mode = sel.getOrDefault(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
            String modeName;
            switch (mode.mode()) {
                case "beta", "gamma", "delta" -> modeName = "EMBER";
                case "epsilon", "zeta" -> modeName = "FROST";
                default -> modeName = "ROYAL";
            }
            Class<?> modeCls = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu$Mode");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object modeEnum = Enum.valueOf((Class) modeCls, modeName);
            cir.setReturnValue(modeEnum);
        } catch (Exception e) {
            // 静默
        }
    }

    /**
     * 超限的 getEmberInputSize() 检查 selectedTemplate instanceof BaseMultipleToOneTemplateItem，
     * 创造模板不是该类型，导致返回 0 使 ember 槽位全禁用。改为按 mode 返回正确值。
     */
    @Inject(method = "getEmberInputSize", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$getEmberInputSize(CallbackInfoReturnable<Integer> cir) {
        try {
            java.lang.reflect.Field f = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu")
                    .getDeclaredField("selectedTemplate");
            f.setAccessible(true);
            ItemStack sel = (ItemStack) f.get(this);
            if (ModItems.isCreateTemplate(sel)) {
                CreateTemplateMode m = sel.getOrDefault(
                        ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
                cir.setReturnValue(switch (m.mode()) {
                    case "delta" -> 8;
                    case "gamma" -> 4;
                    case "beta" -> 2;
                    default -> 0;
                });
            }
        } catch (Exception e) {
            // 静默
        }
    }

    /**
     * 超限锻造台 createFrostResult() 原用 `selectedTemplate.getItem() instanceof PermutationTemplateItem`
     * 区分嬗变(Permutation)与形变(Deformation)分支。创造模板是 CreateTemplateItem（非该类型），
     * 导致 ε(嬗变)/ζ(形变) 永远落入 Deformation 分支——ε 匹配不到 Permutation 配方无法合成。
     *
     * 这里用 @Inject HEAD cancellable 完整重写：创造模板 ε→走 Permutation 配方、ζ→走 Deformation
     * 配方；其他情况（非创造模板或非浮霜模式）不 cancel，走原逻辑。
     */
    @Inject(method = "createFrostResult", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$createFrostResult(CallbackInfo ci) {
        try {
            Class<?> clazz = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu");

            Field sf = clazz.getDeclaredField("selectedTemplate");
            sf.setAccessible(true);
            ItemStack sel = (ItemStack) sf.get(this);
            if (sel == null || !ModItems.isCreateTemplate(sel)) return; // 非创造模板走原逻辑

            CreateTemplateMode mode = sel.getOrDefault(
                    ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
            boolean isPermutation = "epsilon".equals(mode.mode());
            boolean isDeformation = "zeta".equals(mode.mode());
            if (!isPermutation && !isDeformation) return; // 不涉及浮霜，走原逻辑

            Field lf = clazz.getDeclaredField("level");
            lf.setAccessible(true);
            Level level = (Level) lf.get(this);
            Field rf = clazz.getDeclaredField("royalFrostInputs");
            rf.setAccessible(true);
            Container inputs = (Container) rf.get(this);

            // 构造 FrostSmithingRecipeInput(selectedTemplate, 输入1, 输入2)
            Class<?> inputCls = Class.forName("dev.dubhe.anvilcraft.recipe.frost.FrostSmithingRecipeInput");
            Object input = inputCls.getConstructor(ItemStack.class, ItemStack.class, ItemStack.class)
                    .newInstance(sel, inputs.getItem(0), inputs.getItem(1));

            Method setFrost = clazz.getDeclaredMethod("setFrostResult", RecipeHolder.class, inputCls);
            setFrost.setAccessible(true);

            Class<?> modRecipeTypes = Class.forName("dev.dubhe.anvilcraft.init.recipe.ModRecipeTypes");
            Object holder = isPermutation
                    ? modRecipeTypes.getField("PERMUTATION_TYPE").get(null)
                    : modRecipeTypes.getField("DEFORMATION_TYPE").get(null);
            Object type = holder.getClass().getMethod("get").invoke(holder);

            RecipeManager rm = level.getRecipeManager();
            Method getRecipesFor = RecipeManager.class.getMethod(
                    "getRecipesFor", RecipeType.class, RecipeInput.class, Level.class);
            @SuppressWarnings("rawtypes")
            List matches = (List) getRecipesFor.invoke(rm, type, input, level);
            if (!matches.isEmpty()) {
                setFrost.invoke(this, matches.get(0), input);
            }
            ci.cancel();
        } catch (Exception e) {
            // 出错则走原逻辑（不 cancel）
        }
    }

    /**
     * 创造模板按 mode 比较选中状态（原 isSelectedTemplate 只比较 itemId，导致所有模式全亮选中）。
     * 当 selected 和当前 stack 都是创造模板时，比较 mode 组件；否则原逻辑保持不变。
     */
    @Inject(method = "isSelectedTemplate", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$isSelectedTemplate(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            java.lang.reflect.Field f = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu")
                    .getDeclaredField("selectedTemplate");
            f.setAccessible(true);
            ItemStack sel = (ItemStack) f.get(this);
            if (ModItems.isCreateTemplate(stack) && ModItems.isCreateTemplate(sel)) {
                CreateTemplateMode m1 = stack.getOrDefault(
                        ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
                CreateTemplateMode m2 = sel.getOrDefault(
                        ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
                cir.setReturnValue(m1.mode().equals(m2.mode()));
            }
        } catch (Exception e) {
            // 静默
        }
    }
}

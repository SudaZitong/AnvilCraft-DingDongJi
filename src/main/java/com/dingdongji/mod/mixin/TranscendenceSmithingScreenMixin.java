package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import com.dingdongji.mod.network.SelectTemplateModePacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 超限锻造台界面（TranscendenceSmithingScreen）：
 * 左键点击模板面板中"展开的创造模板模式条目"时，发送 SelectTemplateModePacket，
 * 并立即在客户端设置 selectedTemplate 以消除点击延迟。
 */
@Pseudo
@Mixin(targets = "dev.dubhe.anvilcraft.client.gui.screen.TranscendenceSmithingScreen", remap = false)
public abstract class TranscendenceSmithingScreenMixin {

    /**
     * 左键点击创造模板模式条目（α~ζ）时，拦截 AnvilCraft 默认的 TemplateAction。
     *
     * <p>AnvilCraft 服务端 handleTemplateAction 用 itemId + findFirst 匹配模板，而我们展开的
     * α~ζ 模式条目 itemId 完全相同，findFirst 永远命中列表第一个（α），导致点任何模式都跳回
     * 普通创造模板。因此点击创造模板模式条目时完全取消原逻辑，改由 SelectTemplateModePacket
     * 按 mode 精确设置 selectedTemplate。右键（收藏）保留给 AnvilCraft。</p>
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$sendSelectedMode(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return; // 仅拦截左键，右键（收藏）留给 AnvilCraft
        try {
            ItemStack hovered = ddj$templateAt(mouseX, mouseY);
            if (hovered == null || hovered.isEmpty()) return;
            if (!ModItems.isCreateTemplate(hovered)) return;
            CreateTemplateMode mode = hovered.getOrDefault(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
            int containerId = screen.getMenu().containerId;
            PacketDistributor.sendToServer(new SelectTemplateModePacket(containerId, mode.mode()));

            // 客户端立即更新 selectedTemplate，消除"需点两次"的延迟并让高亮立即正确
            try {
                Object menu = screen.getMenu();
                Class<?> tc = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu");
                if (tc.isInstance(menu)) {
                    Field f = tc.getDeclaredField("selectedTemplate");
                    f.setAccessible(true);
                    ItemStack clientSel = hovered.copyWithCount(1);
                    f.set(menu, clientSel);
                }
            } catch (Exception ex) {
                // 静默
            }

            // 取消 AnvilCraft 默认的 TemplateAction（按 itemId 匹配会跳回 α）
            cir.setReturnValue(true);
        } catch (Exception e) {
            // 静默
        }
    }

    /**
     * 创造模板的收藏（favorite）按 itemId 判断，所有模式同 ID 导致全变黄。
     * 对创造模板返回 false，不显示收藏高亮。
     */
    @Inject(method = "isFavorite", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$isFavorite(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ModItems.isCreateTemplate(stack)) {
            cir.setReturnValue(false);
        }
    }

    /** 反射调用 anvilcraft 的 private templateAt(x, y) 获取鼠标悬停的模板。 */
    private ItemStack ddj$templateAt(double x, double y) {
        try {
            Class<?> cls = this.getClass();
            while (cls != null && cls != Object.class) {
                try {
                    Method m = cls.getDeclaredMethod("templateAt", double.class, double.class);
                    m.setAccessible(true);
                    return (ItemStack) m.invoke(this, x, y);
                } catch (NoSuchMethodException ns) {
                    cls = cls.getSuperclass();
                }
            }
        } catch (Exception e) {
            // 静默
        }
        return null;
    }
}

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

import java.lang.reflect.Method;

/**
 * 皇家钢 / 余烬 / 浮霜 锻造台界面（共用 AdjacentSmithingScreen）：
 * 点击模板面板中"展开的创造模板模式条目"时，额外发送 SelectTemplateModePacket，
 * 让服务端把模板槽里借入的创造模板切换为该模式。
 * 在 anvilcraft 原有借用逻辑（发送 Action 借用创造模板）之后发送，保证服务端先借入再设模式。
 */
@Pseudo
@Mixin(targets = "dev.dubhe.anvilcraft.client.gui.screen.AdjacentSmithingScreen", remap = false)
public abstract class AdjacentSmithingScreenMixin {

    /**
     * 左键点击创造模板模式条目（α~ζ）时，拦截 AnvilCraft 默认的 handleTemplateAction。
     *
     * <p>AnvilCraft 服务端 handleTemplateAction 用 itemId 判断，创造模板各模式 itemId 相同，
     * 一旦已借入就会把「再点任意模式」误判为「归还模板」，导致无法切换模式（跳回/失效）。
     * 因此点击创造模板模式条目时完全取消原逻辑，改由 SelectTemplateModePacket 按 mode 处理，
     * 服务端统一完成借入 + 设模式（皇家固定普通模式 α）。右键（收藏）保留给 AnvilCraft。</p>
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void ddj$sendSelectedMode(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return; // 仅拦截左键，右键（收藏）留给 AnvilCraft
        try {
            ItemStack hovered = ddj$templateAt(mouseX, mouseY);
            if (hovered == null || hovered.isEmpty()) return;
            if (!ModItems.isCreateTemplate(hovered)) return;
            CreateTemplateMode mode = hovered.getOrDefault(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
            int containerId = ((AbstractContainerScreen<?>) (Object) this).getMenu().containerId;
            PacketDistributor.sendToServer(new SelectTemplateModePacket(containerId, mode.mode()));
            // 取消 AnvilCraft 默认点击（按 itemId 匹配，会干扰模式切换）
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

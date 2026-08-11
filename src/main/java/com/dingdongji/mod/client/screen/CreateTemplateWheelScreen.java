package com.dingdongji.mod.client.screen;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import com.dingdongji.mod.network.SwitchTemplateModePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * 创造模板模式切换轮盘 — 仿铁砧工艺共振器/多用途工具轮盘。
 * ALT 按住展开，松开确认选择。
 */
public class CreateTemplateWheelScreen extends Screen {

    private final InteractionHand hand;
    public WheelWidget wheel;
    private static List<ModeEntry> cachedModes = null;
    private static List<WheelWidget.SectionBuilder> cachedBuilders = null;

    public CreateTemplateWheelScreen(InteractionHand hand) {
        super(Component.literal("Select Template Mode"));
        this.hand = hand;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static List<ModeEntry> getModes() {
        if (cachedModes != null) return cachedModes;

        boolean hasEZ = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.parse("anvilcraft:permutation_smithing_template")
        ) != net.minecraft.world.item.Items.AIR;

        cachedModes = new ArrayList<>();
        cachedBuilders = new ArrayList<>();

        addMode(CreateTemplateMode.ALPHA, "\u03B1 \u666E\u901A");
        addMode(CreateTemplateMode.BETA, "\u03B2 \u4E8C\u5408\u4E00");
        addMode(CreateTemplateMode.GAMMA, "\u03B3 \u56DB\u5408\u4E00");
        addMode(CreateTemplateMode.DELTA, "\u03B4 \u516B\u5408\u4E00");
        if (hasEZ) {
            addMode(CreateTemplateMode.EPSILON, "\u03B5 \u5B17\u53D8");
            addMode(CreateTemplateMode.ZETA, "\u03B6 \u5F62\u53D8");
        }

        return cachedModes;
    }

    private static void addMode(CreateTemplateMode mode, String label) {
        cachedModes.add(new ModeEntry(mode, label));
        ItemStack icon = new ItemStack(ModItems.CREATE_TEMPLATE.get());
        icon.set(ModComponents.CREATE_TEMPLATE_MODE.get(), mode);
        cachedBuilders.add(new WheelWidget.SectionBuilder(
                Component.literal(label),
                (graphics, x, y, w, h) -> graphics.renderItem(icon, x, y, 9910597)
        ));
    }

    @Override
    protected void init() {
        this.clearWidgets();
        getModes();

        int size = 140;
        int leftPos = (this.width - size) / 2;
        int topPos = (this.height - size) / 2;

        WheelWidget w = new WheelWidget(leftPos, topPos, size, cachedBuilders)
                .setCurrentIndex(this.wheel != null ? this.wheel.getSelectedIndex() : 0);
        w.open();
        this.wheel = this.addRenderableWidget(w);
    }

    @Override
    public void removed() {
        super.removed();
        if (wheel == null) return;
        int index = wheel.getSelectedIndex();
        if (index >= 0 && index < cachedModes.size()) {
            PacketDistributor.sendToServer(
                    new SwitchTemplateModePacket(hand, cachedModes.get(index).mode().mode())
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    /** 尝试打开轮盘（由 KeyBindingHandler 调用） */
    public static void tryOpen(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!stack.is(ModItems.CREATE_TEMPLATE.get())) {
            hand = InteractionHand.OFF_HAND;
            stack = mc.player.getOffhandItem();
        }
        if (!stack.is(ModItems.CREATE_TEMPLATE.get())) return;

        // 每 200 tick 清除缓存
        if (mc.level != null && mc.level.getGameTime() % 200 == 0) {
            cachedModes = null;
            cachedBuilders = null;
        }

        mc.setScreen(new CreateTemplateWheelScreen(hand));
    }

    private record ModeEntry(CreateTemplateMode mode, String label) {}
}

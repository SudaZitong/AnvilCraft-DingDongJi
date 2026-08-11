package com.dingdongji.mod.client.screen;

import com.dingdongji.mod.inventory.JiAnvilMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 叽砧 GUI — 参照铁砧工艺 TranscendenceAnvilScreen 实现。
 * 覆盖 renderLabels 显示真实消耗，不检查"过于昂贵"。
 * 保留原版铁砧的命名输入框功能。
 */
public class JiAnvilScreen extends ItemCombinerScreen<JiAnvilMenu> {

    private static final ResourceLocation ANVIL_LOCATION =
        ResourceLocation.withDefaultNamespace("textures/gui/container/anvil.png");

    private static final ResourceLocation TEXT_FIELD =
        ResourceLocation.withDefaultNamespace("container/anvil/text_field");
    private static final ResourceLocation TEXT_FIELD_DISABLED =
        ResourceLocation.withDefaultNamespace("container/anvil/text_field_disabled");

    private final Player player;
    private EditBox name;

    public JiAnvilScreen(JiAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ANVIL_LOCATION);
        this.player = playerInventory.player;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
        this.name.setEditable(false);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String string = this.name.getValue();
        this.init(minecraft, width, height);
        this.name.setValue(string);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        if (this.name.keyPressed(keyCode, scanCode, modifiers) || this.name.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onNameChanged(String name) {
        Slot slot = this.menu.getSlot(0);
        if (!slot.hasItem()) return;
        String string = name;
        if (!slot.getItem().has(DataComponents.CUSTOM_NAME)
            && string.equals(slot.getItem().getHoverName().getString())) {
            string = "";
        }
        if (this.menu.setItemName(string) && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.connection.send(new ServerboundRenameItemPacket(string));
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        ResourceLocation bg = this.menu.getSlot(0).getItem().isEmpty()
            ? TEXT_FIELD_DISABLED : TEXT_FIELD;
        guiGraphics.blitSprite(bg, this.leftPos + 59, this.topPos + 20, 110, 16);
    }

    @Override
    public void renderFg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        int cost = this.menu.getCost();
        if (cost > 0) {
            Component component;
            int color = 0x80FF20;
            if (this.menu.getSlot(2).hasItem()) {
                component = Component.translatable("container.repair.cost", cost);
                if (!this.menu.getSlot(2).mayPickup(this.player)) {
                    color = 0xFF6060;
                }
            } else {
                component = null;
            }
            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                guiGraphics.fill(k - 2, 67, this.imageWidth - 8, 79, 0x4F000000);
                guiGraphics.drawString(this.font, component, k, 69, color);
            }
        }
    }

    @Override
    protected void renderErrorIcon(GuiGraphics guiGraphics, int x, int y) {
        // 空实现：不画错误图标，避免遮挡箭头
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (dataSlotIndex == 0) {
            this.name.setValue(stack.isEmpty() ? "" : stack.getHoverName().getString());
            this.name.setEditable(!stack.isEmpty());
            this.setFocused(this.name);
        }
    }
}

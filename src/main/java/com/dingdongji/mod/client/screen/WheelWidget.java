package com.dingdongji.mod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

/**
 * 简洁轮盘 — 无圆环、无发光，仅显示物品图标和文字标签。
 */
public class WheelWidget extends AbstractWidget {

    private static final float RADIUS = 48f;
    private static final int ANIM_MS = 150;
    private static final int CLOSE_MS = 100;

    private final Minecraft mc = Minecraft.getInstance();
    private final Vector2f center;
    private final List<Section> sections = new ArrayList<>();

    private long openTime;
    private boolean opening = false;
    private boolean closing = false;
    private int selectedIndex = 0;

    public record Section(Component name, SectionRenderer renderer, Vector2f pos) {}

    @FunctionalInterface
    public interface SectionRenderer {
        void render(GuiGraphics graphics, int x, int y, int w, int h);
    }

    public WheelWidget(int x, int y, int size, List<SectionBuilder> builders) {
        super(x, y, size, size, Component.empty());
        this.center = new Vector2f(x + size / 2f, y + size / 2f);

        float degreeEach = 360f / builders.size();
        for (int i = 0; i < builders.size(); i++) {
            SectionBuilder b = builders.get(i);
            float rad = (float) Math.toRadians(degreeEach * i);
            float sx = center.x + (float) Math.sin(rad) * RADIUS;
            float sy = center.y - (float) Math.cos(rad) * RADIUS;
            sections.add(new Section(b.name(), b.renderer(), new Vector2f(sx, sy)));
        }
    }

    public WheelWidget setCurrentIndex(int index) {
        if (index >= 0 && index < sections.size()) this.selectedIndex = index;
        return this;
    }

    public int getSelectedIndex() { return selectedIndex; }
    public boolean isClosing() { return closing; }

    public void open() {
        this.openTime = System.currentTimeMillis();
        this.opening = true;
        this.closing = false;
    }

    public int close() {
        if (closing) return selectedIndex;
        this.openTime = System.currentTimeMillis();
        this.opening = false;
        this.closing = true;
        return selectedIndex;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (closing) return false;
        if (scrollY > 0) selectedIndex = (selectedIndex + 1) % sections.size();
        else if (scrollY < 0) selectedIndex = (selectedIndex - 1 + sections.size()) % sections.size();
        return true;
    }

    private void updateHover(double mouseX, double mouseY) {
        if (closing) return;
        float bestDist = Float.MAX_VALUE;
        int bestIdx = -1;
        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            float dx = (float) mouseX - s.pos().x;
            float dy = (float) mouseY - s.pos().y;
            float dist = dx * dx + dy * dy;
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }
        if (bestIdx >= 0 && bestDist < 400) { // 20px 半径
            selectedIndex = bestIdx;
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateHover(mouseX, mouseY);
        long elapsed = System.currentTimeMillis() - openTime;

        float progress;
        if (closing) {
            progress = Math.max(0, 1f - elapsed / (float) CLOSE_MS);
            progress = easeOutCubic(progress);
            renderSections(guiGraphics, progress);
            if (progress <= 0) mc.setScreen(null);
            return;
        }
        if (!opening) return;
        progress = Math.min(1f, elapsed / (float) ANIM_MS);
        progress = easeOutCubic(progress);
        renderSections(guiGraphics, progress);
    }

    private static float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    private void renderSections(GuiGraphics guiGraphics, float progress) {
        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            float sx = (s.pos().x - center.x) * progress + center.x;
            float sy = (s.pos().y - center.y) * progress + center.y;
            boolean hovered = (i == selectedIndex);

            // 物品图标
            s.renderer().render(guiGraphics, (int) sx - 8, (int) sy - 8, 16, 16);

            // 文字标签
            String label = s.name().getString();
            int tw = mc.font.width(label);
            float tx = sx - tw / 2f;
            float ty = sy + 11;
            int alpha = Math.min(255, (int) (progress * 255));
            int color = hovered ? 0xFFFFFF : 0xAAAAAA;
            int argb = (alpha << 24) | (color & 0x00FFFFFF);
            guiGraphics.drawString(mc.font, label, (int) tx, (int) ty, argb, false);
        }
    }

    public static class SectionBuilder {
        private final Component name;
        private final SectionRenderer renderer;
        public SectionBuilder(Component name, SectionRenderer renderer) {
            this.name = name;
            this.renderer = renderer;
        }
        public Component name() { return name; }
        public SectionRenderer renderer() { return renderer; }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}

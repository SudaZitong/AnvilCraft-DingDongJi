package com.dingdongji.mod.input;

import com.dingdongji.mod.client.screen.CreateTemplateWheelScreen;
import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import com.dingdongji.mod.network.SwitchTemplateModePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端 tick 处理器，用于检测自定义键位按下并发送网络包。
 * 同时处理 ALT 键打开创造模板轮盘（使用铁砧工艺 WheelWidget，与共振器一致）。
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class KeyBindingHandler {

    /** ALT 键是否已打开轮盘（防止每 tick 重复打开）*/
    private static boolean altWheelOpened = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 处理 V/C/Z 键位
        ModKeyBindings.tick(mc);

        // 处理 ALT 键打开创造模板轮盘（与铁砧工艺共振器一致）
        long window = mc.getWindow().getWindow();
        boolean altDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

        if (altDown && !altWheelOpened && mc.screen == null) {
            if (mc.player.getMainHandItem().is(ModItems.CREATE_TEMPLATE.get())
                    || mc.player.getOffhandItem().is(ModItems.CREATE_TEMPLATE.get())) {
                CreateTemplateWheelScreen.tryOpen(mc.player.getMainHandItem());
                altWheelOpened = true;
            }
        } else if (!altDown) {
            altWheelOpened = false;
            // ALT 释放时由 onKeyInput 处理（调用 wheel.onClosing()）
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // ALT 键释放时触发轮盘关闭 + 动画 + 确认选择
        if ((event.getKey() == GLFW.GLFW_KEY_LEFT_ALT || event.getKey() == GLFW.GLFW_KEY_RIGHT_ALT)
                && event.getAction() == GLFW.GLFW_RELEASE) {
            altWheelOpened = false;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof CreateTemplateWheelScreen screen) {
                screen.wheel.close();
            }
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Post event) {
        // 左键按下时检测创造模板（轮盘打开时不处理，避免误判）
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT || event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        // 检查手持物品是否为创造模板
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);
            if (!stack.is(ModItems.CREATE_TEMPLATE.get())) continue;

            CreateTemplateMode current = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
            if (current == null || CreateTemplateMode.ALPHA.equals(current)) return;

            // 左键切回普通模板并同步到服务端
            stack.set(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.ALPHA);
            PacketDistributor.sendToServer(new SwitchTemplateModePacket(hand, CreateTemplateMode.ALPHA.mode()));
            return;
        }
    }
}

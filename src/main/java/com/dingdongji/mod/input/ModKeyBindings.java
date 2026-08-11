package com.dingdongji.mod.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.dingdongji.mod.network.AbilityTogglePacket;
import com.dingdongji.mod.network.GlowingVisionTogglePacket;
import com.dingdongji.mod.network.NeutronBarrierTogglePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 叮咚叽自定义键位。
 * - V 键：靴子能力切换（蹈火/蹈虚）
 * - C 键：超限合金头盔高亮切换
 */
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.dingdongji";
    public static final String ABILITY_NAME = "key.dingdongji.ability_toggle";
    public static final String GLOWING_NAME = "key.dingdongji.glowing_vision_toggle";
    public static final String NEUTRON_BARRIER_NAME = "key.dingdongji.neutron_barrier_toggle";

    public static final KeyMapping ABILITY_KEY = new KeyMapping(
            ABILITY_NAME,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static final KeyMapping GLOWING_VISION_KEY = new KeyMapping(
            GLOWING_NAME,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
    );

    public static final KeyMapping NEUTRON_BARRIER_KEY = new KeyMapping(
            NEUTRON_BARRIER_NAME,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY
    );

    /** 注册键位映射（MOD 总线事件） */
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ABILITY_KEY);
        event.register(GLOWING_VISION_KEY);
        event.register(NEUTRON_BARRIER_KEY);
    }

    /**
     * 在客户端 tick 中检测按键按下，发送网络包到服务端。
     */
    public static void tick(Minecraft mc) {
        while (ABILITY_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new AbilityTogglePacket(false));
        }
        while (GLOWING_VISION_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new GlowingVisionTogglePacket());
        }
        while (NEUTRON_BARRIER_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new NeutronBarrierTogglePacket());
        }
    }
}

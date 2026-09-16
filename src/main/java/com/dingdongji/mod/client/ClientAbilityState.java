package com.dingdongji.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端能力开关镜像：夜视/蹈火需要在客户端物理与光照计算中即时可读。
 * 由按键乐观更新，并由服务端 AbilityStateSyncPacket 校准。
 */
public final class ClientAbilityState {
    private ClientAbilityState() {}

    public static boolean lavaWalker;
    public static int helmetMode = 5;

    public static boolean nightVision() {
        return helmetMode == 2 || helmetMode == 4;
    }

    public static boolean isLavaWalker(Player player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == player && lavaWalker;
    }

    public static boolean isLocalNightVision(Player player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == player && nightVision();
    }

    public static void applySync(boolean lavaWalkerEnabled, int mode) {
        lavaWalker = lavaWalkerEnabled;
        helmetMode = mode;
    }
}

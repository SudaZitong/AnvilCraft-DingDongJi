package com.dingdongji.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S 网络包：超限合金头盔高亮敌对生物开关。
 */
public record GlowingVisionTogglePacket() implements CustomPacketPayload {

    public static final Type<GlowingVisionTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("dingdongji", "glowing_vision_toggle"));
    public static final StreamCodec<FriendlyByteBuf, GlowingVisionTogglePacket> STREAM_CODEC =
            StreamCodec.unit(new GlowingVisionTogglePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

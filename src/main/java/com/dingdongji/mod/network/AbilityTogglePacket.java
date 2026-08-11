package com.dingdongji.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S 网络包：靴子能力切换。
 * 服务端根据当前穿着自动判断执行蹈火（余烬靴子）还是蹈虚（超限靴子）。
 */
public record AbilityTogglePacket(boolean doublePress) implements CustomPacketPayload {

    public static final Type<AbilityTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("dingdongji", "ability_toggle"));

    public static final StreamCodec<FriendlyByteBuf, AbilityTogglePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeBoolean(packet.doublePress),
                    buf -> new AbilityTogglePacket(buf.readBoolean())
            );

    /** 兼容旧用法 */
    public AbilityTogglePacket() {
        this(false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

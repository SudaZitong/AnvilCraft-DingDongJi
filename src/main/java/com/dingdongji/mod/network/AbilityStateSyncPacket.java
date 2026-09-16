package com.dingdongji.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 服务端 → 客户端：同步蹈火开关与头盔适应模式，供客户端夜视/液面行走使用。
 */
public record AbilityStateSyncPacket(boolean lavaWalker, int helmetMode) implements CustomPacketPayload {

    public static final Type<AbilityStateSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("dingdongji", "ability_state_sync"));

    public static final StreamCodec<FriendlyByteBuf, AbilityStateSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeBoolean(packet.lavaWalker);
                        buf.writeVarInt(packet.helmetMode);
                    },
                    buf -> new AbilityStateSyncPacket(buf.readBoolean(), buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

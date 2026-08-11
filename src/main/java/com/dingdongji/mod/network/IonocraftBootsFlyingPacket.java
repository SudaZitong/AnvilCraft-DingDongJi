package com.dingdongji.mod.network;

import com.dingdongji.mod.client.IonocraftBootsClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 超限靴子蹈虚飞行状态同步包（Server → Client）。
 * 当玩家开始/停止蹈虚（超限靴子创造飞行）飞行时通知周边客户端，
 * 供客户端为视野内所有蹈虚飞行玩家本地渲染粒子（复刻飘升机机制）。
 */
public record IonocraftBootsFlyingPacket(int playerId, boolean flying) implements CustomPacketPayload {

    public static final Type<IonocraftBootsFlyingPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("dingdongji", "ionocraft_boots_flying"));

    public static final StreamCodec<FriendlyByteBuf, IonocraftBootsFlyingPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeVarInt(packet.playerId);
                        buf.writeBoolean(packet.flying);
                    },
                    buf -> new IonocraftBootsFlyingPacket(buf.readVarInt(), buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

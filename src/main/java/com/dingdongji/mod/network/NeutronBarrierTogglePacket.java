package com.dingdongji.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S 网络包：超限合金护腿中子屏障开关。
 */
public record NeutronBarrierTogglePacket() implements CustomPacketPayload {

    public static final Type<NeutronBarrierTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("dingdongji", "neutron_barrier_toggle"));
    public static final StreamCodec<FriendlyByteBuf, NeutronBarrierTogglePacket> STREAM_CODEC =
            StreamCodec.unit(new NeutronBarrierTogglePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.dingdongji.mod.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 凌霜组件 - 浮霜靴子：可在细雪上行走，并无视方块摩擦
 */
public record FrostWalkComponent() {
    public static final FrostWalkComponent INSTANCE = new FrostWalkComponent();
    public static final Codec<FrostWalkComponent> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, FrostWalkComponent> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}

package com.dingdongji.mod.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 傲雪组件 - 浮霜头盔：无视冰冻伤害
 */
public record FrostWardComponent() {
    public static final FrostWardComponent INSTANCE = new FrostWardComponent();
    public static final Codec<FrostWardComponent> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, FrostWardComponent> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}

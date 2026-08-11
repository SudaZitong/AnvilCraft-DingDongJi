package com.dingdongji.mod.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 无义标记组件。
 * 穿戴浮霜金属全套时，禁用除诅咒外的所有魔咒，将其转换为护甲值与盔甲韧性。
 */
public record MeaninglessComponent() {

    public static final MeaninglessComponent DEFAULT = new MeaninglessComponent();
    public static final MapCodec<MeaninglessComponent> CODEC = MapCodec.unit(DEFAULT);
    public static final StreamCodec<ByteBuf, MeaninglessComponent> STREAM_CODEC = StreamCodec.unit(DEFAULT);
}

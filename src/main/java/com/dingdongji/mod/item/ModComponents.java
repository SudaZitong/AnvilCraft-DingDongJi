package com.dingdongji.mod.item;

import com.dingdongji.mod.KryptonMod;
import com.dingdongji.mod.item.component.*;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, KryptonMod.MODID);

    // ===== 武器组件 =====

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DevourData>> DEVOUR =
            COMPONENTS.register("devour", () -> DataComponentType.<DevourData>builder()
                    .persistent(DevourData.CODEC)
                    .networkSynchronized(DevourData.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccumulateData>> ACCUMULATE =
            COMPONENTS.register("accumulate", () -> DataComponentType.<AccumulateData>builder()
                    .persistent(AccumulateData.CODEC)
                    .networkSynchronized(AccumulateData.STREAM_CODEC)
                    .build()
            );

    // ===== 超限合金套组件 =====

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlowingVisionComponent>> GLOWING_VISION =
            COMPONENTS.register("glowing_vision", () -> DataComponentType.<GlowingVisionComponent>builder()
                    .persistent(GlowingVisionComponent.CODEC)
                    .networkSynchronized(GlowingVisionComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BarrierIIComponent>> BARRIER_II =
            COMPONENTS.register("barrier_ii", () -> DataComponentType.<BarrierIIComponent>builder()
                    .persistent(BarrierIIComponent.CODEC)
                    .networkSynchronized(BarrierIIComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NeutronBarrierComponent>> NEUTRON_BARRIER =
            COMPONENTS.register("neutron_barrier", () -> DataComponentType.<NeutronBarrierComponent>builder()
                    .persistent(NeutronBarrierComponent.CODEC)
                    .networkSynchronized(NeutronBarrierComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MeaninglessData>> MEANINGLESS_DATA =
            COMPONENTS.register("meaningless_data",
                    () -> DataComponentType.<MeaninglessData>builder()
                            .persistent(MeaninglessData.CODEC.codec())
                            .networkSynchronized(MeaninglessData.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MeaninglessComponent>> MEANINGLESS =
            COMPONENTS.register("meaningless",
                    () -> DataComponentType.<MeaninglessComponent>builder()
                            .persistent(MeaninglessComponent.CODEC.codec())
                            .networkSynchronized(MeaninglessComponent.STREAM_CODEC)
                            .build()
            );

    // ===== 皇家钢套组件 =====

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RoyalSteelAffinityComponent>> ROYAL_STEEL_AFFINITY =
            COMPONENTS.register("royal_steel_affinity", () -> DataComponentType.<RoyalSteelAffinityComponent>builder()
                    .persistent(RoyalSteelAffinityComponent.CODEC)
                    .networkSynchronized(RoyalSteelAffinityComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ComfortableComponent>> COMFORTABLE =
            COMPONENTS.register("comfortable", () -> DataComponentType.<ComfortableComponent>builder()
                    .persistent(ComfortableComponent.CODEC)
                    .networkSynchronized(ComfortableComponent.STREAM_CODEC)
                    .build()
            );

    // ===== 余烬金属套组件 =====

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HeatInsulationComponent>> HEAT_INSULATION =
            COMPONENTS.register("heat_insulation", () -> DataComponentType.<HeatInsulationComponent>builder()
                    .persistent(HeatInsulationComponent.CODEC)
                    .networkSynchronized(HeatInsulationComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BarrierIComponent>> BARRIER_I =
            COMPONENTS.register("barrier_i", () -> DataComponentType.<BarrierIComponent>builder()
                    .persistent(BarrierIComponent.CODEC)
                    .networkSynchronized(BarrierIComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EmberRegenComponent>> EMBER_REGEN =
            COMPONENTS.register("ember_regen", () -> DataComponentType.<EmberRegenComponent>builder()
                    .persistent(EmberRegenComponent.CODEC)
                    .networkSynchronized(EmberRegenComponent.STREAM_CODEC)
                    .build()
            );

    // ===== 创造模板组件 =====

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CreateTemplateMode>> CREATE_TEMPLATE_MODE =
            COMPONENTS.register("create_template_mode", () -> DataComponentType.<CreateTemplateMode>builder()
                    .persistent(CreateTemplateMode.CODEC)
                    .networkSynchronized(CreateTemplateMode.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LavaWalkerComponent>> LAVA_WALKER =
            COMPONENTS.register("lava_walker", () -> DataComponentType.<LavaWalkerComponent>builder()
                    .persistent(LavaWalkerComponent.CODEC)
                    .networkSynchronized(LavaWalkerComponent.STREAM_CODEC)
                    .build()
            );

}

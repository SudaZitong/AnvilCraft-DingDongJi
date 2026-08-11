package com.dingdongji.mod.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 创造模板的当前模式。
 */
public record CreateTemplateMode(String mode) {
    public static final Codec<CreateTemplateMode> CODEC = Codec.STRING.xmap(CreateTemplateMode::new, CreateTemplateMode::mode);
    public static final StreamCodec<ByteBuf, CreateTemplateMode> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(CreateTemplateMode::new, CreateTemplateMode::mode);

    // α 普通模板（可替代所有锻造模板）
    public static final CreateTemplateMode ALPHA = new CreateTemplateMode("alpha");
    // β 二合一锻造模板
    public static final CreateTemplateMode BETA = new CreateTemplateMode("beta");
    // γ 四合一锻造模板
    public static final CreateTemplateMode GAMMA = new CreateTemplateMode("gamma");
    // δ 八合一锻造模板
    public static final CreateTemplateMode DELTA = new CreateTemplateMode("delta");
    // ε 嬗变模板（1.6）
    public static final CreateTemplateMode EPSILON = new CreateTemplateMode("epsilon");
    // ζ 形变模板（1.6）
    public static final CreateTemplateMode ZETA = new CreateTemplateMode("zeta");

    public static final CreateTemplateMode DEFAULT = ALPHA;

    public CreateTemplateMode next() {
        return switch (mode) {
            case "alpha" -> BETA;
            case "beta" -> GAMMA;
            case "gamma" -> DELTA;
            case "delta" -> hasEpsilonZeta() ? EPSILON : ALPHA;
            case "epsilon" -> hasEpsilonZeta() ? ZETA : ALPHA;
            case "zeta" -> ALPHA;
            default -> ALPHA;
        };
    }

    private static boolean hasEpsilonZeta() {
        // 检查铁砧工艺 1.6 的模板物品是否存在
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.parse("anvilcraft:permutation_smithing_template"));
        return item != net.minecraft.world.item.Items.AIR;
    }

    public String getDisplayName() {
        return switch (mode) {
            case "alpha" -> "锻造模板-\u03B1";
            case "beta" -> "锻造模板-\u03B2";
            case "gamma" -> "锻造模板-\u03B3";
            case "delta" -> "锻造模板-\u03B4";
            case "epsilon" -> "锻造模板-\u03B5";
            case "zeta" -> "锻造模板-\u03B6";
            default -> "锻造模板-\u03B1";
        };
    }

    public String getDescription() {
        return switch (mode) {
            case "alpha" -> "普通模板";
            case "beta" -> "二合一锻造模板";
            case "gamma" -> "四合一锻造模板";
            case "delta" -> "八合一锻造模板";
            case "epsilon" -> "嬗变模板";
            case "zeta" -> "形变模板";
            default -> "可替代所有锻造模板";
        };
    }

    public String getTargetTemplateId() {
        return switch (mode) {
            case "beta" -> "anvilcraft:two_to_one_smithing_template";
            case "gamma" -> "anvilcraft:four_to_one_smithing_template";
            case "delta" -> "anvilcraft:eight_to_one_smithing_template";
            case "epsilon" -> "anvilcraft:permutation_smithing_template";
            case "zeta" -> "anvilcraft:deformation_smithing_template";
            default -> null; // alpha: all templates
        };
    }
}

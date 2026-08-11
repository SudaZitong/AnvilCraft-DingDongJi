package com.dingdongji.mod.item.template;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 创造模板 — 手持右键切换模式，不同模式对应不同锻造模板。
 * α: 普通模板（可替代所有）
 * β: 二合一锻造模板
 * γ: 四合一锻造模板
 * δ: 八合一锻造模板
 * ε: 嬗变模板（1.6）
 * ζ: 形变模板（1.6）
 *
 * 直接继承 Item 而非 SmithingTemplateItem，因为 SmithingTemplateItem 的构造器
 * 写死 super(new Item.Properties())，导致传入的 .rarity(EPIC) 等属性被丢弃。
 */
public class CreateTemplateItem extends Item {

    public CreateTemplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        CreateTemplateMode mode = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
        if (mode == null) mode = CreateTemplateMode.DEFAULT;
        return Component.literal(mode.getDisplayName());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        CreateTemplateMode current = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
        if (current == null) current = CreateTemplateMode.DEFAULT;
        CreateTemplateMode next = current.next();
        stack.set(ModComponents.CREATE_TEMPLATE_MODE.get(), next);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CreateTemplateMode mode = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
        if (mode == null) mode = CreateTemplateMode.DEFAULT;

        // 万用描述（彩色粗体）
        tooltip.add(Component.translatable("tooltip.dingdongji.create_template.desc")
                .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x5A07FF)))
                .append(Component.literal(mode.getDescription()).withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0xBC80FF)))));

        // 模板类型切换提示
        tooltip.add(Component.translatable("tooltip.dingdongji.create_template.switch")
                .withStyle(ChatFormatting.GRAY));

        // 全部升级（灰字）
        tooltip.add(Component.translatable("screen.dingdongji.create_template")
                .withStyle(ChatFormatting.GRAY));

        // 空行间隔
        tooltip.add(Component.empty());

        // 可应用于：（灰字）
        tooltip.add(Component.translatable("tooltip.dingdongji.create_template.applies")
                .withStyle(ChatFormatting.GRAY));
        // 所有锻造台配方（蓝字）
        tooltip.add(Component.translatable("screen.dingdongji.create_template.applies_to")
                .withStyle(ChatFormatting.BLUE));

        // 空行间隔
        tooltip.add(Component.empty());

        // 所需原材料：（灰字）
        tooltip.add(Component.translatable("tooltip.dingdongji.create_template.material")
                .withStyle(ChatFormatting.GRAY));
        // 配方所需材料（蓝字）
        tooltip.add(Component.translatable("screen.dingdongji.create_template.upgrade_ingredients")
                .withStyle(ChatFormatting.BLUE));
    }


}

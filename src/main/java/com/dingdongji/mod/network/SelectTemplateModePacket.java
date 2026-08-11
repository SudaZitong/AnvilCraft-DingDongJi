package com.dingdongji.mod.network;

import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端点击模板面板中"展开的创造模板模式条目"时，把所选模式发给服务端。
 * 服务端把当前锻造台菜单的模板槽（皇家/余烬/浮霜为 slot 0）里的创造模板切换为该模式。
 * 超限锻造台模板不进入槽位，反射设置 selectedTemplate 的模式，由其 getMode() 切换到对应配方模式。
 */
public record SelectTemplateModePacket(int containerId, String mode) implements CustomPacketPayload {

    public static final Type<SelectTemplateModePacket> TYPE = new Type<>(
            ResourceLocation.parse("dingdongji:select_template_mode")
    );

    private static final ResourceLocation CREATE_TEMPLATE_ID =
            ResourceLocation.parse("dingdongji:create_template");

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectTemplateModePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SelectTemplateModePacket::containerId,
                    ByteBufCodecs.STRING_UTF8, SelectTemplateModePacket::mode,
                    SelectTemplateModePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectTemplateModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            AbstractContainerMenu menu = serverPlayer.containerMenu;
            if (menu == null || menu.containerId != packet.containerId()) return;
            applyMode(menu, packet.mode(), serverPlayer);
        });
    }

    /**
     * 皇家/余烬/浮霜：模板槽是 slot 0。若尚未借入则先反射调用 AnvilCraft 的 borrowTemplate
     * 从周围容器借入创造模板，再按其模式设置；皇家锻造台固定为普通模式 α（其他模式对皇家
     * 无意义）。
     * 超限：模板不进入槽位，反射设置 selectedTemplate 的模式，getMode() 会据此切配方模式。
     */
    private static void applyMode(AbstractContainerMenu menu, String mode, ServerPlayer serverPlayer) {
        // ===== 皇家 / 余烬 / 浮霜（AdjacentSmithingMenu 体系）=====
        try {
            Class<?> adj = Class.forName("dev.dubhe.anvilcraft.inventory.AdjacentSmithingMenu");
            if (adj.isInstance(menu)) {
                boolean isRoyal = Class.forName("dev.dubhe.anvilcraft.inventory.RoyalSmithingMenu")
                        .isInstance(menu);
                // 皇家锻造台固定普通模式 α
                String targetMode = isRoyal ? "alpha" : mode;

                // 1) 模板槽未借入创造模板时，先反射调用 AnvilCraft 的 borrowTemplate 借入
                if (!ModItems.isCreateTemplate(menu.getSlot(0).getItem())) {
                    try {
                        java.lang.reflect.Method borrow = adj.getDeclaredMethod(
                                "borrowTemplate", ServerPlayer.class, ResourceLocation.class);
                        borrow.setAccessible(true);
                        borrow.invoke(menu, serverPlayer, CREATE_TEMPLATE_ID);
                    } catch (Exception ex) {
                        // 容器中没有可借入的创造模板
                    }
                }

                // 2) 设置模板槽创造模板的 mode（皇家固定 α）
                ItemStack template = menu.getSlot(0).getItem();
                if (ModItems.isCreateTemplate(template)) {
                    template.set(ModComponents.CREATE_TEMPLATE_MODE.get(), new CreateTemplateMode(targetMode));
                }
                // 3) 同步 borrowedTemplateStack 的 mode，保证面板高亮一致
                try {
                    java.lang.reflect.Field bf = adj.getDeclaredField("borrowedTemplateStack");
                    bf.setAccessible(true);
                    Object bo = bf.get(menu);
                    if (bo instanceof ItemStack bs && ModItems.isCreateTemplate(bs)) {
                        bs.set(ModComponents.CREATE_TEMPLATE_MODE.get(), new CreateTemplateMode(targetMode));
                    }
                } catch (Exception ex) {
                    // 静默
                }

                menu.slotsChanged(menu.getSlot(0).container);
                menu.broadcastChanges();
                return;
            }
        } catch (Exception e) {
            // 静默
        }

        // 超限：强制设置 selectedTemplate 为带正确 mode 的创造模板
        // 优先从模板列表中找到 mode 匹配的条目；找不到则复制当前 selectedTemplate 并 set mode
        try {
            Class<?> tc = Class.forName("dev.dubhe.anvilcraft.inventory.TranscendenceSmithingMenu");
            if (tc.isInstance(menu)) {
                ItemStack target = null;
                // 1) 优先从模板列表按 mode 精确匹配
                try {
                    java.lang.reflect.Field tf = tc.getDeclaredField("templates");
                    tf.setAccessible(true);
                    Object templatesObj = tf.get(menu);
                    if (templatesObj instanceof java.util.List<?> templates) {
                        for (Object o : templates) {
                            if (o instanceof ItemStack t && ModItems.isCreateTemplate(t)) {
                                CreateTemplateMode m = t.getOrDefault(ModComponents.CREATE_TEMPLATE_MODE.get(), CreateTemplateMode.DEFAULT);
                                if (m.mode().equals(mode)) { target = t; break; }
                            }
                        }
                    }
                } catch (Exception ex) { /* 忽略 */ }

                java.lang.reflect.Field sf = tc.getDeclaredField("selectedTemplate");
                sf.setAccessible(true);

                // 2) 若列表未展开/未匹配，则复制当前 selectedTemplate 作为基础并 set mode
                if (target == null) {
                    Object cur = sf.get(menu);
                    if (cur instanceof ItemStack cs && ModItems.isCreateTemplate(cs)) {
                        target = cs.copyWithCount(1);
                    }
                }

                if (target != null) {
                    target.set(ModComponents.CREATE_TEMPLATE_MODE.get(), new CreateTemplateMode(mode));
                    sf.set(menu, target.copyWithCount(1));
                    // 触发超限的配方重新匹配
                    menu.broadcastChanges();
                    // 尝试触发输入容器重新匹配
                    try { menu.slotsChanged(menu.getSlot(0).container); } catch (Exception ex) {}
                    try { menu.slotsChanged(menu.getSlot(3).container); } catch (Exception ex) {}
                }
            }
        } catch (Exception e) {
            // 静默
        }
    }
}

package com.dingdongji.mod.client;

import com.dingdongji.mod.KryptonMod;
import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.item.component.CreateTemplateMode;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端初始化：注册创造模板的 ItemProperties，用于模型 override 切换。
 */
public class ClientSetupHandler {

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.CREATE_TEMPLATE.get(),
                    ResourceLocation.fromNamespaceAndPath(KryptonMod.MODID, "mode"),
                    (stack, level, entity, seed) -> {
                        CreateTemplateMode mode = stack.get(ModComponents.CREATE_TEMPLATE_MODE.get());
                        if (mode == null) mode = CreateTemplateMode.DEFAULT;
                        return switch (mode.mode()) {
                            case "alpha" -> 0.0f;
                            case "beta" -> 1.0f;
                            case "gamma" -> 2.0f;
                            case "delta" -> 3.0f;
                            case "epsilon" -> 4.0f;
                            case "zeta" -> 5.0f;
                            default -> 0.0f;
                        };
                    }
            );
        });
    }

    /**
     * 注册粒子提供者（在 RegisterParticleProvidersEvent 中调用）。
     */
    public static void registerParticleProviders(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                com.dingdongji.mod.init.ModParticles.IONOCRAFT_BOOTS_EXHAUST.get(),
                com.dingdongji.mod.client.particle.IonocraftBootsExhaustParticle.Provider::new
        );
        event.registerSpriteSet(
                com.dingdongji.mod.init.ModParticles.NEUTRON_BARRIER_REPEL.get(),
                com.dingdongji.mod.client.particle.NeutronBarrierParticle.Provider::new
        );
        event.registerSpriteSet(
                com.dingdongji.mod.init.ModParticles.NEUTRON_BARRIER_ABSORB.get(),
                com.dingdongji.mod.client.particle.NeutronBarrierParticle.Provider::new
        );
    }
}

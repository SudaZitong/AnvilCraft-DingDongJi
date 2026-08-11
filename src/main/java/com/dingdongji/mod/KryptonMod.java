package com.dingdongji.mod;

import com.dingdongji.mod.init.ModParticles;

import com.dingdongji.mod.block.ModBlocks;
import com.dingdongji.mod.client.screen.JiAnvilScreen;
import com.dingdongji.mod.inventory.JiAnvilMenu;
import com.dingdongji.mod.event.ModArmorSetHandler;
import com.dingdongji.mod.event.ModEvents;
import com.dingdongji.mod.event.ModRecipeHandler;
import com.dingdongji.mod.event.ModifyDefaultComponentsHandler;
import com.dingdongji.mod.item.ModArmorMaterials;
import com.dingdongji.mod.item.ModComponents;
import com.dingdongji.mod.item.ModItems;
import com.dingdongji.mod.input.ModKeyBindings;
import com.dingdongji.mod.network.ModNetwork;
import com.dingdongji.mod.tab.ModCreativeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(KryptonMod.MODID)
public class KryptonMod {
    public static final String MODID = "dingdongji";

    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public KryptonMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModComponents.COMPONENTS.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);

        // ===== 手动注册事件处理器（避免 @EventBusSubscriber 自动扫描失败）=====

        // MOD 总线事件
        modEventBus.addListener(ModifyDefaultComponentsHandler::onModifyDefaultComponents);
        modEventBus.addListener(ModKeyBindings::registerKeyMappings);
        modEventBus.addListener(com.dingdongji.mod.client.ClientSetupHandler::onClientSetup);
        modEventBus.addListener(com.dingdongji.mod.client.ClientSetupHandler::registerParticleProviders);

        // ===== 注册叽砧 Screen =====
        modEventBus.addListener((RegisterMenuScreensEvent event) -> {
            @SuppressWarnings("unchecked")
            MenuType<JiAnvilMenu> type = (MenuType<JiAnvilMenu>) ModMenuTypes.JI_ANVIL.get();
            event.register(type, JiAnvilScreen::new);
        });



        // ===== 注册网络包 =====
        modEventBus.addListener(ModNetwork::register);

        // GAME 总线事件
        var gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(ModArmorSetHandler::onPlayerTick);
        gameBus.addListener(ModArmorSetHandler::onLivingDamage);
        gameBus.addListener(ModArmorSetHandler::onPlayerLoggedOut);
        gameBus.addListener(ModArmorSetHandler::onPlayerLogin);
        gameBus.addListener(ModArmorSetHandler::onEquipmentChange);
        gameBus.addListener(ModEvents::onTooltip);
        gameBus.addListener(ModEvents::onLivingDeath);
        gameBus.addListener(ModEvents::onItemAttributeModifier);
        gameBus.addListener(ModRecipeHandler::onServerStarted);

        // ===== 创造模板支持（通过 Mixin 实现）=====
    }
}

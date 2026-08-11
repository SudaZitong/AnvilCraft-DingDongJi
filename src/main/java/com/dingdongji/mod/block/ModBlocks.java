package com.dingdongji.mod.block;

import com.dingdongji.mod.KryptonMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(KryptonMod.MODID);

    /** 叽砧音效：使用原版铁砧音效，仅下落坠地音效由 Mixin 改为鸡叫 */
    private static final SoundType JI_ANVIL_SOUND_TYPE = SoundType.ANVIL;

    public static final DeferredBlock<JiAnvilBlock> JI_ANVIL =
            BLOCKS.register("ji_anvil",
                    () -> new JiAnvilBlock(BlockBehaviour.Properties.of()
                            .strength(5.0f, 1200.0f)
                            .requiresCorrectToolForDrops()
                            .sound(JI_ANVIL_SOUND_TYPE)
                            .noOcclusion()
                    )
            );

    /** 叽块 */
    public static final DeferredBlock<KejiBlock> KEJI_BLOCK =
            BLOCKS.register("keji_block",
                    () -> new KejiBlock(BlockBehaviour.Properties.of()
                            .strength(10.0f, 3.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                    )
            );

    // BlockItem helper
    public static Supplier<BlockItem> createBlockItem(DeferredBlock<? extends Block> block) {
        return () -> new BlockItem(block.get(), new Item.Properties());
    }
}

package com.dingdongji.mod.mixin;

import com.dingdongji.mod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {

    @Shadow
    public abstract BlockState getBlockState();

    @Shadow
    public boolean cancelDrop;

    private boolean dingdongji$isJiAnvil = false;

    /**
     * 检查当前下落方块是否为叽砧。
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void checkJiAnvil(CallbackInfo ci) {
        BlockState bs = this.getBlockState();
        this.dingdongji$isJiAnvil = bs != null && bs.is(ModBlocks.JI_ANVIL.get());
    }

    /**
     * 静默化叽砧的放置音效：替换 setBlockAndUpdate 为静默 setBlock。
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
        )
    )
    private boolean silenceAnvilPlaceSound(Level level, BlockPos pos, BlockState state) {
        if (this.dingdongji$isJiAnvil) {
            return level.setBlock(pos, state, 18); // 18 = 无方块更新 + 无音效
        }
        return level.setBlockAndUpdate(pos, state);
    }

    /**
     * 叽砧落地时播放鸡咕咕叫音效，阻止原版碎裂音效。
     */
    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Fallable;onLand(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/item/FallingBlockEntity;)V"
        )
    )
    private void onJiAnvilLand(CallbackInfo ci) {
        if (!this.dingdongji$isJiAnvil) return;

        FallingBlockEntity self = (FallingBlockEntity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            // 播放鸡咕咕叫音效
            serverLevel.playSound(
                null,
                self.blockPosition(),
                SoundEvents.CHICKEN_AMBIENT,
                SoundSource.BLOCKS,
                1.0F, 1.0F
            );
        }
        // 阻止原版铁砧碎裂音效
        this.cancelDrop = true;
    }

    /**
     * 静默叽砧的铁砧落地音效（levelEvent 1030），因为摔落音效和放置音效是一体的。
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"
        )
    )
    private void silenceAnvilLandEvent(Level level, int eventId, BlockPos pos, int data) {
        if (this.dingdongji$isJiAnvil) {
            // 不播放铁砧落地音效（1030）和碎裂音效（1029），因为摔落音效和放置音效是一体的
            return;
        }
        level.levelEvent(eventId, pos, data);
    }
}

package com.dingdongji.mod.mixin;

import com.dingdongji.mod.item.ModItems;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * 连锁锻造台中，创造模板所在的槽位不会被从模板槽列表中移除，
 * 从而实现一个创造模板即可完成所有链式升级步骤。
 */
@Mixin(targets = "dev.anvilcraft.pigsplus.inventory.ChainSmithingMenu")
public abstract class ChainSmithingMenuMixin {

    @Redirect(
        method = "findAndRemoveUsedSlots",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;remove(Ljava/lang/Object;)Z",
            ordinal = 0
        ),
        remap = false
    )
    private boolean redirectTemplateSlotRemove(List<Integer> list, Object o) {
        if (o instanceof Integer slotIndex) {
            ItemStack stack = ((ItemCombinerMenu) (Object) this).getSlot(slotIndex).getItem();
            if (stack.is(ModItems.CREATE_TEMPLATE.get())) {
                return false;
            }
        }
        return list.remove(o);
    }
}

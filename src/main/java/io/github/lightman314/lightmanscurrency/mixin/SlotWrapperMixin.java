package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public class SlotWrapperMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(Slot target, int index, int x, int y, CallbackInfo callbackInfo)
    {
        if(target instanceof WalletSlot && this instanceof SlotAccessor accessor)
        {
            ScreenPosition pos = LCConfig.CLIENT.walletSlotCreative.get();
            accessor.setX(pos.x + 1);
            accessor.setY(pos.y + 1);
        }
    }

}

package io.github.lightman314.lightmanscurrency.mixin.client;

/*import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.mixin.common.SlotMixin;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

//@Mixin(CreativeModeInventoryScreen.class)*/
public class CreativeScreenMixin
{
    /*@Inject(method = "setCurrentCreativeTab", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private void patchBackpackSlot(CreativeModeTab tab, CallbackInfo ci)
    {
    	CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        screen.getContainer().inventorySlots.stream().filter(slot -> (slot.inventory instanceof IWalletInventory) && slot.getSlotIndex() == ExtendedPlayerInventory.WALLETINDEX).findFirst().ifPresent(slot -> {
            ((SlotMixin) slot).setXPos(153);
            ((SlotMixin) slot).setYPos(33);
        });
    }*/
}

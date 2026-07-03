package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

    @Unique
    private Slot lightmanscurrency$walletSlot = null;

    @Unique
    private InventoryMenu lightmanscurrency$self() { return (InventoryMenu)(Object)this; }

    @Inject(method = "<init>",at = @At("TAIL"))
    private void openMenu(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        //TODO ignore if curios is loaded
        if(this.lightmanscurrency$self() instanceof AbstractContainerMenuAccessor accessor)
        {
            ScreenPosition pos = LCConfig.CLIENT.walletSlot.get().offset(1,1);
            this.lightmanscurrency$walletSlot = accessor.addCustomSlot(new WalletSlot(inventory.player,pos.x,pos.y));
        }
    }

    @Inject(at = @At("HEAD"),method = "quickMoveStack",cancellable = true)
    private void quickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if(this.lightmanscurrency$walletSlot == null)
            return;
        //Only quick move from the items slots
        if(slotIndex >= 9 && slotIndex < 45)
        {
            Slot slot = this.lightmanscurrency$self().slots.get(slotIndex);
            if(slot.hasItem() && WalletItem.isWallet(slot.getItem()) && !this.lightmanscurrency$walletSlot.hasItem())
            {
                ItemStack item = slot.getItem();
                this.lightmanscurrency$walletSlot.set(item.copyWithCount(1));
                item.shrink(1);
                if(item.isEmpty())
                    slot.set(ItemStack.EMPTY);
                else
                    slot.setChanged();
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }


}
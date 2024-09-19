package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.attachments.wallet.WalletHelpers;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

    @Unique
    private Slot walletSlot = null;

    @Unique
    private InventoryMenu self() { return (InventoryMenu)(Object)this; }

    @Accessor("owner")
    protected abstract Player getPlayer();

    @Inject(at = @At("TAIL"), method = "<init>")
    protected void init(Inventory inventory, boolean active, final Player player, CallbackInfo callbackInfo)
    {
        //Don't add wallet slot if curios is installed
        if(LCCurios.isLoaded())
            return;
        if(this.self() instanceof AbstractContainerMenuAccessor accessor)
            this.walletSlot = accessor.addCustomSlot(new WalletSlot(player, WalletHelpers.getWalletContainer(player), 0, LCConfig.CLIENT.walletSlot.get().x + 1, LCConfig.CLIENT.walletSlot.get().y + 1));
    }

    @Inject(at = @At("HEAD"), method = "quickMoveStack", cancellable = true)
    protected void quickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> callbackInfo)
    {
        //Ignored if curios is installed
        if(LCCurios.isLoaded())
            return;
        //Only quick move from the inventory slots
        if(slotIndex >= 9 && slotIndex < 45 && this.walletSlot != null)
        {
            Slot slot = this.self().slots.get(slotIndex);
            if(slot.hasItem() && WalletItem.isWallet(slot.getItem()) && !this.walletSlot.hasItem())
            {
                this.walletSlot.set(slot.getItem().copy());
                slot.set(ItemStack.EMPTY);
                callbackInfo.setReturnValue(ItemStack.EMPTY);
            }
        }
    }

}
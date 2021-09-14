package io.github.lightman314.lightmanscurrency.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {
	
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(method = "processCreativeInventoryAction", at = @At(value = "TAIL"))
	private void patchWalletAction(CCreativeInventoryActionPacket packetIn, CallbackInfo ci)
	{
		if(!this.player.isCreative())
            return;
		
        ItemStack stack = packetIn.getStack();
        int maxSize = this.player.container.inventorySlots.size();
        int minSlot = 45;
        if(LightmansCurrency.isBackpackedLoaded())
        	minSlot++;
        if(packetIn.getSlotId() <= minSlot || packetIn.getSlotId() >= maxSize)
            return;
        
        Slot slot = this.player.container.getSlot(packetIn.getSlotId());
        if(!(slot instanceof WalletSlot))
            return;
        
        boolean changed = stack.isEmpty() || stack.getDamage() >= 0 && stack.getCount() <= 64;
        if(changed)
        {
            if(stack.isEmpty())
            {
                this.player.container.putStackInSlot(packetIn.getSlotId(), ItemStack.EMPTY);
            }
            else
            {
                this.player.container.putStackInSlot(packetIn.getSlotId(), stack);
            }
            this.player.container.setCanCraft(this.player, true);
            this.player.container.detectAndSendChanges();
        }
        
	}

}

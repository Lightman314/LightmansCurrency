package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.items.WalletItem;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class WalletSlot extends Slot{
	
	public static final ResourceLocation EMPTY_WALLET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_wallet_slot");
	
	public WalletSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
        return isValidWallet(stack);
	}
	
	public static boolean isValidWallet(ItemStack stack) {
		return stack.getItem() instanceof WalletItem;
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_WALLET_SLOT);
	}
	

}

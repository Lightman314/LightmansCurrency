package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.items.WalletItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class WalletSlot extends Slot{
	
	public static final ResourceLocation EMPTY_WALLET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_wallet_slot");
	
	public WalletSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
        return isValidWallet(stack);
	}
	
	public static boolean isValidWallet(ItemStack stack) {
		return stack.getItem() instanceof WalletItem;
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_WALLET_SLOT);
	}
	

}

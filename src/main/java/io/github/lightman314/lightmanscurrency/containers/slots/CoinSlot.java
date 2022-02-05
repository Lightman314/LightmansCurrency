package io.github.lightman314.lightmanscurrency.containers.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CoinSlot extends Slot{
	
	public static final ResourceLocation EMPTY_COIN_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_coin_slot");
	
	private boolean acceptHiddenCoins;
	
	public CoinSlot(IInventory inventory, int index, int x, int y)
	{
		this(inventory, index, x, y, true);
	}
	
	public CoinSlot(IInventory inventory, int index, int x, int y, boolean acceptHiddenCoins)
	{
		super(inventory, index, x, y);
		this.acceptHiddenCoins = acceptHiddenCoins;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		if(acceptHiddenCoins)
			return MoneyUtil.isCoin(stack.getItem());
		else
			return MoneyUtil.isCoin(stack.getItem()) && !MoneyUtil.isCoinHidden(stack.getItem());
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_COIN_SLOT);
	}

}

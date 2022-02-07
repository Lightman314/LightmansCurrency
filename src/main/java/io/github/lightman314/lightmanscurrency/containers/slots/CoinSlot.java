package io.github.lightman314.lightmanscurrency.containers.slots;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CoinSlot extends Slot{
	
	public static final ResourceLocation EMPTY_COIN_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_coin_slot");
	
	private boolean acceptHiddenCoins;
	
	private boolean lockInput = false;
	public void LockInput() { this.lockInput = true; }
	public void UnlockInput() { this.lockInput = false; }
	private boolean lockOutput = false;
	public void LockOutput() { this.lockOutput = true; }
	public void UnlockOutput() { this.lockOutput = false; }
	public void Lock() { this.lockInput = this.lockOutput = true; }
	public void Unlock() { this.lockInput = this.lockOutput = false; }
	
	private List<ICoinSlotListener> listeners = Lists.newArrayList();
	
	public CoinSlot(IInventory inventory, int index, int x, int y)
	{
		this(inventory, index, x, y, true);
	}
	
	public CoinSlot(IInventory inventory, int index, int x, int y, boolean acceptHiddenCoins)
	{
		super(inventory, index, x, y);
		this.acceptHiddenCoins = acceptHiddenCoins;
	}
	
	public CoinSlot addListener(ICoinSlotListener listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);
		return this;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		if(this.lockInput)
			return false;
		if(acceptHiddenCoins)
			return MoneyUtil.isCoin(stack.getItem());
		else
			return MoneyUtil.isCoin(stack.getItem()) && !MoneyUtil.isCoinHidden(stack.getItem());
	}
	
	@Override
	public void putStack(ItemStack stack) {
		if(this.lockInput && !stack.isEmpty())
			return;
		super.putStack(stack);
	}
	
	@Override
	public ItemStack decrStackSize(int amount) {
		if(this.lockOutput)
			return ItemStack.EMPTY;
		return super.decrStackSize(amount);
	}
	
	@Override
	public boolean canTakeStack(PlayerEntity playerIn) {
		if(this.lockOutput)
			return false;
		return super.canTakeStack(playerIn);
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_COIN_SLOT);
	}
	
	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		this.listeners.forEach(listener -> listener.onCoinSlotChanged());
	}
	
	public interface ICoinSlotListener {
		public void onCoinSlotChanged();
	}

}

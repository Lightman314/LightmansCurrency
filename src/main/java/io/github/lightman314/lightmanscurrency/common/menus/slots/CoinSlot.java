package io.github.lightman314.lightmanscurrency.common.menus.slots;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class CoinSlot extends SimpleSlot{
	
	public static final ResourceLocation EMPTY_COIN_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_coin_slot");
	
	private final boolean acceptHiddenCoins;
	
	private boolean lockInput = false;
	public void LockInput() { this.lockInput = true; }
	public void UnlockInput() { this.lockInput = false; }
	private boolean lockOutput = false;
	public void LockOutput() { this.lockOutput = true; }
	public void UnlockOutput() { this.lockOutput = false; }
	public void Lock() { this.lockInput = this.lockOutput = true; }
	public void Unlock() { this.lockInput = this.lockOutput = false; }
	
	private final List<ICoinSlotListener> listeners = Lists.newArrayList();
	
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
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if(lockInput)
			return false;
		if(acceptHiddenCoins)
			return MoneyUtil.isCoin(stack.getItem());
		else
			return MoneyUtil.isCoin(stack.getItem()) && !MoneyUtil.isCoinHidden(stack.getItem());
	}
	
	@Override
	public void set(@Nonnull ItemStack stack) {
		if(this.lockInput && !stack.isEmpty())
			return;
		super.set(stack);
	}
	
	@Nonnull
	@Override
	public ItemStack remove(int amount) {
		if(this.lockOutput)
			return ItemStack.EMPTY;
		return super.remove(amount);
	}
	
	@Override
	public boolean mayPickup(@Nonnull PlayerEntity player) {
		if(this.lockOutput)
			return false;
		return super.mayPickup(player);
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(PlayerContainer.BLOCK_ATLAS, EMPTY_COIN_SLOT);
	}
	
	public void setChanged() {
		super.setChanged();
		this.listeners.forEach(ICoinSlotListener::onCoinSlotChanged);
	}
	
	public interface ICoinSlotListener {
		void onCoinSlotChanged();
	}

}

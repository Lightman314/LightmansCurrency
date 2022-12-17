package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CoinSlot extends SimpleSlot{
	
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
	
	public CoinSlot(Container inventory, int index, int x, int y)
	{
		this(inventory, index, x, y, true);
	}
	
	public CoinSlot(Container inventory, int index, int x, int y, boolean acceptHiddenCoins)
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
	public boolean mayPlace(@NotNull ItemStack stack) {
		if(lockInput)
			return false;
		if(acceptHiddenCoins)
			return MoneyUtil.isCoin(stack.getItem());
		else
			return MoneyUtil.isCoin(stack.getItem()) && !MoneyUtil.isCoinHidden(stack.getItem());
	}
	
	@Override
	public void set(ItemStack stack) {
		if(this.lockInput && !stack.isEmpty())
			return;
		super.set(stack);
	}
	
	@Override
	public @NotNull ItemStack remove(int amount) {
		if(this.lockOutput)
			return ItemStack.EMPTY;
		return super.remove(amount);
	}
	
	@Override
	public boolean mayPickup(@NotNull Player player) {
		if(this.lockOutput)
			return false;
		return super.mayPickup(player);
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_COIN_SLOT);
	}
	
	public void setChanged() {
		super.setChanged();
		this.listeners.forEach(listener -> listener.onCoinSlotChanged());
	}
	
	public interface ICoinSlotListener {
		public void onCoinSlotChanged();
	}

}

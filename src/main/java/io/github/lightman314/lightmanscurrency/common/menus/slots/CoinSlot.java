package io.github.lightman314.lightmanscurrency.common.menus.slots;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CoinSlot extends SimpleSlot{
	
	public static final ResourceLocation EMPTY_COIN_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_coin_slot");
	
	private final boolean acceptSideChains;
	
	private final List<ICoinSlotListener> listeners = Lists.newArrayList();
	
	public CoinSlot(Container inventory, int index, int x, int y)
	{
		this(inventory, index, x, y, true);
	}
	
	public CoinSlot(Container inventory, int index, int x, int y, boolean acceptSideChains)
	{
		super(inventory, index, x, y);
		this.acceptSideChains = acceptSideChains;
	}
	
	public CoinSlot addListener(ICoinSlotListener listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);
		return this;
	}
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) { return CoinAPI.isCoin(stack, this.acceptSideChains); }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_COIN_SLOT);
	}
	
	public void setChanged() {
		super.setChanged();
		this.listeners.forEach(ICoinSlotListener::onCoinSlotChanged);
	}
	
	public interface ICoinSlotListener {
		void onCoinSlotChanged();
	}

}

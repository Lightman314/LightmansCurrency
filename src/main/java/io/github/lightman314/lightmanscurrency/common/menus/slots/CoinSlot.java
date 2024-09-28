package io.github.lightman314.lightmanscurrency.common.menus.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CoinSlot extends EasySlot {
	
	public static final ResourceLocation EMPTY_COIN_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_coin_slot");
	
	private final boolean acceptSideChains;
	
	public CoinSlot(Container inventory, int index, int x, int y)
	{
		this(inventory, index, x, y, true);
	}
	
	public CoinSlot(Container inventory, int index, int x, int y, boolean acceptSideChains)
	{
		super(inventory, index, x, y);
		this.acceptSideChains = acceptSideChains;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) { return CoinAPI.API.IsCoin(stack, this.acceptSideChains); }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_COIN_SLOT);
	}

}

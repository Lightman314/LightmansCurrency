package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class CoinSlot extends EasySlot {
	
	public static final ResourceLocation EMPTY_COIN_SLOT = LightmansCurrency.id("item/empty_coin_slot");
	
	private final boolean acceptSideChains;
	
	public CoinSlot(IItemHandlerModifiable inventory, int index, int x, int y) { this(inventory, index, x, y, true); }
	
	public CoinSlot(IItemHandlerModifiable inventory, int index, int x, int y, boolean acceptSideChains)
	{
		super(inventory, index, x, y);
		this.acceptSideChains = acceptSideChains;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) { return super.mayPlace(stack) && CoinAPI.getApi().IsAllowedInCoinContainer(stack,this.acceptSideChains); }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_COIN_SLOT); }
	
	public interface ICoinSlotListener {  void onCoinSlotChanged(); }

}

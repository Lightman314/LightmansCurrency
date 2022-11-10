package io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemTradeRestriction {

	public static final ResourceLocation NO_RESTRICTION_KEY = new ResourceLocation(LightmansCurrency.MODID, "none");

	public static void init() {
		register(NO_RESTRICTION_KEY, NONE);
		register("equipment_head", EquipmentRestriction.HEAD);
		register("equipment_chest", EquipmentRestriction.CHEST);
		register("equipment_legs", EquipmentRestriction.LEGS);
		register("equipment_feet", EquipmentRestriction.FEET);
		register("ticket_kiosk", TicketKioskRestriction.NONE);
	}

	private static final Map<ResourceLocation,ItemTradeRestriction> registeredRestrictions = new HashMap<>();

	private static void register(String type, ItemTradeRestriction restriction) { register(new ResourceLocation(LightmansCurrency.MODID, type), restriction); }

	public static void register(ResourceLocation type, ItemTradeRestriction restriction) {
		if(registeredRestrictions.containsKey(type))
		{
			LightmansCurrency.LogWarning("Cannot register an Item Trade Restriction of type '" + type + "' as one is already registered.");
			return;
		}
		registeredRestrictions.put(type, restriction);
	}

	public static ResourceLocation getId(ItemTradeRestriction restriction) {
		if(restriction == null || !registeredRestrictions.containsValue(restriction))
			return NO_RESTRICTION_KEY;

		AtomicReference<ResourceLocation> result = new AtomicReference<>(NO_RESTRICTION_KEY);
		registeredRestrictions.forEach((type, r) -> { if(r == restriction) result.set(type); });
		return result.get();

	}

	public static void forEach(BiConsumer<ResourceLocation,ItemTradeRestriction> consumer) { registeredRestrictions.forEach(consumer); }

	public static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation(LightmansCurrency.MODID, "items/empty_item_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, DEFAULT_BACKGROUND);
	
	public static final ItemTradeRestriction NONE = new ItemTradeRestriction();

	protected ItemTradeRestriction() { }
	
	public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade) { return sellItem; }
	
	public boolean allowSellItem(ItemStack itemStack) { return true; }
	
	public ItemStack filterSellItem(ItemStack itemStack) { return itemStack; }
	
	public boolean allowItemSelectItem(ItemStack itemStack) { return true; }
	
	public boolean allowExtraItemInStorage(ItemStack itemStack) { return false; }
	
	public int getSaleStock(TraderItemStorage traderStorage, ItemStack... sellItemList) {
		int minStock = Integer.MAX_VALUE;
		for(ItemStack sellItem : InventoryUtil.combineQueryItems(sellItemList))
			minStock = Math.min(this.getItemStock(sellItem, traderStorage), minStock);
		return minStock;
	}
	
	protected final int getItemStock(ItemStack sellItem, TraderItemStorage traderStorage)
	{
		if(sellItem.isEmpty())
			return Integer.MAX_VALUE;
		return traderStorage.getItemCount(sellItem) / sellItem.getCount();
	}
	
	public void removeItemsFromStorage(TraderItemStorage traderStorage, ItemStack... sellItemList)
	{
		for(ItemStack sellItem : sellItemList)
			this.removeFromStorage(sellItem, traderStorage);
	}
	
	protected final void removeFromStorage(ItemStack sellItem, TraderItemStorage traderStorage)
	{
		if(sellItem.isEmpty())
			return;
		traderStorage.removeItem(sellItem);
	}
	
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG() { return BACKGROUND; }
	
}

package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemTradeRestriction {

	public static final ResourceLocation NO_RESTRICTION_KEY = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "none");

	public static void init() {
		register(NO_RESTRICTION_KEY, NONE);
		register("equipment_head", EquipmentRestriction.HEAD);
		register("equipment_chest", EquipmentRestriction.CHEST);
		register("equipment_legs", EquipmentRestriction.LEGS);
		register("equipment_feet", EquipmentRestriction.FEET);
		register("ticket_kiosk", TicketKioskRestriction.INSTANCE);
		register("book", BookRestriction.INSTANCE);
	}

	private static final Map<ResourceLocation,ItemTradeRestriction> registeredRestrictions = new HashMap<>();

	private static void register(String type, ItemTradeRestriction restriction) { register(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, type), restriction); }

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

	public static final ItemTradeRestriction NONE = new ItemTradeRestriction();

	protected ItemTradeRestriction() { }

	public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade) { return sellItem; }

	public boolean allowSellItem(ItemStack itemStack) { return true; }

	public ItemStack filterSellItem(ItemStack itemStack) { return itemStack; }

	public boolean allowItemSelectItem(ItemStack itemStack) { return true; }

	public boolean allowExtraItemInStorage(ItemStack itemStack) { return false; }

	public int getSaleStock(TraderItemStorage traderStorage, ItemTradeData trade) {
		int minStock = Integer.MAX_VALUE;
		//New method of checking stock
		for(ItemRequirement requirement : InventoryUtil.combineRequirements(trade.getItemRequirement(0), trade.getItemRequirement(1)))
			minStock = Math.min(this.getItemStock(requirement, traderStorage), minStock);
		return minStock;
	}

	public List<ItemStack> getRandomSellItems(ItemTraderData trader, ItemTradeData trade)
	{
		if(this.alwaysEnforceNBT(0) && this.alwaysEnforceNBT(1))
			return this.getNBTEnforcedSellItems(trade);
		List<ItemStack> randomItems = ItemRequirement.getRandomItemsMatchingRequirements(trader.getStorage(), trade.getItemRequirement(0), trade.getItemRequirement(1));
		if(randomItems == null && trader.isCreative()) //If creative, return nbt enforced version if no random items are present in the inventory.
			return this.getNBTEnforcedSellItems(trade);
		return randomItems;
	}

	/**
	 * Original method for getting the items being sold.
	 * Used when this always enforces NBT.
	 * Also used for creative traders when the items in storage could not be found.
	 */
	protected final List<ItemStack> getNBTEnforcedSellItems(ItemTradeData trade) {
		List<ItemStack> results = new ArrayList<>();
		for(int i = 0; i < 2; ++i)
		{
			ItemStack stack = trade.getSellItem(i);
			if(!stack.isEmpty())
				results.add(stack);
		}
		return results;
	}

	protected final int getItemStock(ItemRequirement requirement, TraderItemStorage traderStorage)
	{
		if(requirement.isNull() || requirement.getCount() == 0)
			return Integer.MAX_VALUE;
		return traderStorage.getItemCount(requirement) / requirement.getCount();
	}

	protected final int getItemStock(ItemStack sellItem, TraderItemStorage traderStorage)
	{
		if(sellItem.isEmpty())
			return Integer.MAX_VALUE;
		return traderStorage.getItemCount(sellItem) / sellItem.getCount();
	}

	public void removeItemsFromStorage(TraderItemStorage traderStorage, List<ItemStack> soldItems)
	{
		for(ItemStack sellItem : soldItems)
			this.removeFromStorage(sellItem, traderStorage);
	}

	protected final void removeFromStorage(ItemStack sellItem, TraderItemStorage traderStorage)
	{
		if(sellItem.isEmpty())
			return;
		traderStorage.removeItem(sellItem);
	}

	public boolean alwaysEnforceNBT(int tradeSlot) { return false; }

	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG() { return EasySlot.BACKGROUND; }

}
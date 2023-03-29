package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.IBarterTrade;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.client.ItemTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemTradeData extends TradeData implements IBarterTrade {
	
	public ItemTradeData(boolean validateRules) { super(validateRules); this.resetNBTList(); }
	
	public enum ItemTradeType { SALE(0,1), PURCHASE(1,2), BARTER(2,0);
		public final int index;
		private final int nextIndex;
		public final ItemTradeType next() { return fromIndex(this.nextIndex); }
		ItemTradeType(int index, int nextIndex) { this.index = index; this.nextIndex = nextIndex; }
		public static ItemTradeType fromIndex(int index) {
			for(ItemTradeType type : ItemTradeType.values())
			{
				if(type.index == index)
					return type;
			}
			return ItemTradeType.SALE;
		}
	}
	
	ItemTradeRestriction restriction = ItemTradeRestriction.NONE;
	SimpleContainer items = new SimpleContainer(4);
	final List<Boolean> enforceNBT = Lists.newArrayList(true, true, true, true);
	private void resetNBTList() { for(int i = 0; i < 4; ++i) this.enforceNBT.set(i, true); }
	ItemTradeType tradeType = ItemTradeType.SALE;
	String customName1 = "";
	String customName2 = "";
	
	public ItemStack getSellItem(int index)
	{
		if(index >= 0 && index < 2)
			return this.restriction.modifySellItem(this.items.getItem(index).copy(), this.getCustomName(index), this);
		return ItemStack.EMPTY;
	}

	public List<ItemStack> getRandomSellItems(ItemTraderData trader) { return this.restriction.getRandomSellItems(trader, this); }

	public ItemStack getBarterItem(int index)
	{
		if(index >= 0 && index < 2)
			return this.items.getItem(index + 2).copy();
		return ItemStack.EMPTY;
	}
	
	public ItemStack getItem(int index)
	{
		if(index >= 0 && index < 2)
			return this.getSellItem(index);
		else if(index >= 2 && index < 4)
			return this.getBarterItem(index - 2);
		return ItemStack.EMPTY;
	}
	
	public void setItem(ItemStack itemStack, int index)
	{
		if(index >= 0 && index < 4)
		{
			if(index < 2)
			{
				if(this.restriction.allowSellItem(itemStack) || itemStack.isEmpty())
					this.items.setItem(index, this.restriction.filterSellItem(itemStack).copy());
			}
			else
				this.items.setItem(index, itemStack.copy());
		}
		else
			LightmansCurrency.LogError("Cannot define the item trades item at index " + index + ". Must be between 0-3!");
	}

	public boolean alwaysEnforcesNBT(int slot) { return this.restriction.alwaysEnforceNBT(slot); }

	public boolean getEnforceNBT(int slot) {
		if(slot >= 0 && slot < 4)
			return this.enforceNBT.get(slot) || this.alwaysEnforcesNBT(slot);
		return true;
	}

	public void setEnforceNBT(int slot, boolean newValue) {
		if(slot >= 0 && slot < 4)
			this.enforceNBT.set(slot, newValue || this.alwaysEnforcesNBT(slot));
	}

	public ItemRequirement getItemRequirement(int slot) {
		if(slot >= 0 && slot < 4)
		{
			if(this.getEnforceNBT(slot))
				return ItemRequirement.of(this.getItem(slot));
			else
				return ItemRequirement.ofItemNoNBT(this.getItem(slot));
		}
		return ItemRequirement.NULL;
	}

	public boolean allowItemInStorage(ItemStack item) {
		for(int i = 0; i < (this.isBarter() ? 4 : 2); ++i)
		{
			if(this.getItemRequirement(i).test(item))
				return true;
		}
		return this.restriction.allowExtraItemInStorage(item);
	}

	public boolean shouldStorageItemBeSaved(ItemStack item) {
		if((this.isSale() || this.isBarter()) && this.isValid())
		{
			//Only loop through sale items, as purchase items don't matter as far as storage is concerned.
			for(int i = 0; i < 2; ++i)
			{
				if(!this.getEnforceNBT(i) && this.getItemRequirement(i).test(item))
					return true;
			}
		}
		return false;
	}
	
	public boolean hasCustomName(int index) { return !this.getCustomName(index).isEmpty(); }
	
	public String getCustomName(int index)
	{
		return switch (index) {
			case 0 -> this.customName1;
			case 1 -> this.customName2;
			default -> "";
		};
	}
	
	public void setCustomName(int index, String customName)
	{
		switch (index) {
			case 0 -> this.customName1 = customName;
			case 1 -> this.customName2 = customName;
		}
	}
	
	@Override
	public TradeDirection getTradeDirection()
	{
		return switch (this.tradeType) {
			case SALE -> TradeDirection.SALE;
			case PURCHASE -> TradeDirection.PURCHASE;
			default -> TradeDirection.NONE;
		};
	}
	
	public ItemTradeType getTradeType() { return this.tradeType; }
	
	public boolean isSale() { return this.tradeType == ItemTradeType.SALE; }
	public boolean isPurchase() { return this.tradeType == ItemTradeType.PURCHASE; }
	public boolean isBarter() { return this.tradeType == ItemTradeType.BARTER; }
	
	public void setTradeType(ItemTradeType tradeDirection) { this.tradeType = tradeDirection; this.validateRuleStates(); }
	
	public ItemTradeRestriction getRestriction() { return this.restriction; }
	
	public void setRestriction(ItemTradeRestriction restriction) { this.restriction = restriction; }
	
	@Override
	public boolean isValid()
	{
		if(this.tradeType == ItemTradeType.BARTER)
			return this.sellItemsDefined() && this.barterItemsDefined();
		return super.isValid() && this.sellItemsDefined();
	}
	
	public boolean sellItemsDefined() {
		return !this.getSellItem(0).isEmpty() || !this.getSellItem(1).isEmpty();
	}
	
	public boolean barterItemsDefined() {
		return !this.getBarterItem(0).isEmpty() || !this.getBarterItem(1).isEmpty();
	}
	
	public boolean hasStock(ItemTraderData trader)
	{
		if(!this.sellItemsDefined())
			return false;
		return stockCount(trader) > 0;
	}
	
	public boolean hasStock(TradeContext context)
	{
		if(!this.sellItemsDefined())
			return false;
		return stockCount(context) > 0;
	}
	
	public boolean hasSpace(ItemTraderData trader, List<ItemStack> collectableItems)
	{
		return switch (this.tradeType) {
			case PURCHASE, BARTER -> trader.getStorage().canFitItems(collectableItems);
			default -> true;
		};
	}
	
	public int stockCount(ItemTraderData trader)
	{
		if(!this.sellItemsDefined())
			return 0;
		
		if(this.tradeType == ItemTradeType.PURCHASE)
		{
			if(this.cost.isFree())
				return 1;
			if(this.cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			long price = this.cost.getRawValue();
			return (int)(coinValue / price);
		}
		else if(this.tradeType == ItemTradeType.SALE || this.tradeType == ItemTradeType.BARTER)
		{
			return this.restriction.getSaleStock(trader.getStorage(), this);
		}
		else //Other types are not handled yet.
			return 0;
	}
	
	public int stockCount(TradeContext context)
	{
		if(!this.sellItemsDefined())
			return 0;
		
		if(!context.hasTrader() || !(context.getTrader() instanceof ItemTraderData trader))
			return 0;

		if(trader.isCreative())
			return 1;
		
		if(this.tradeType == ItemTradeType.PURCHASE)
		{
			if(this.cost.isFree())
				return 1;
			if(this.cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			long price = this.getCost(context).getRawValue();
			return (int)(coinValue / price);
		}
		else if(this.tradeType == ItemTradeType.SALE || this.tradeType == ItemTradeType.BARTER)
		{
			return this.restriction.getSaleStock(trader.getStorage(), this);
		}
		else //Other types are not handled yet.
			return 0;
	}
	
	public boolean canAfford(TradeContext context) {
		if(this.isSale())
			return context.hasFunds(this.getCost(context));
		if(this.isPurchase())
			return context.hasItems(this.getItemRequirement(0), this.getItemRequirement(1));
		if(this.isBarter())
			return context.hasItems(this.getItemRequirement(2), this.getItemRequirement(3));
		return false;
	}
	
	public void RemoveItemsFromStorage(TraderItemStorage storage, List<ItemStack> soldItems)
	{
		this.restriction.removeItemsFromStorage(storage, InventoryUtil.combineQueryItems(soldItems));
	}
	
	@Override
	public CompoundTag getAsNBT() {
		CompoundTag tradeNBT = super.getAsNBT();
		InventoryUtil.saveAllItems("Items", tradeNBT, this.items);
		tradeNBT.putString("TradeDirection", this.tradeType.name());
		tradeNBT.putString("CustomName1", this.customName1);
		tradeNBT.putString("CustomName2", this.customName2);
		List<Integer> ignoreNBTSlots = new ArrayList<>();
		for(int i = 0; i < 4; ++i)
		{
			if(!this.getEnforceNBT(i))
				ignoreNBTSlots.add(i);
		}
		if(ignoreNBTSlots.size() > 0)
			tradeNBT.putIntArray("IgnoreNBT", ignoreNBTSlots);
		return tradeNBT;
	}
	
	public static void saveAllData(CompoundTag nbt, List<ItemTradeData> data)
	{
		saveAllData(nbt, data, DEFAULT_KEY);
	}
	
	public static void saveAllData(CompoundTag nbt, List<ItemTradeData> data, String key)
	{
		ListTag listNBT = new ListTag();

		for (ItemTradeData datum : data) {
			listNBT.add(datum.getAsNBT());
		}
		
		nbt.put(key, listNBT);
	}
	
	public static ItemTradeData loadData(CompoundTag compound, boolean validateRules) {
		ItemTradeData trade = new ItemTradeData(validateRules);
		trade.loadFromNBT(compound);
		return trade;
	}
	
	public static List<ItemTradeData> loadAllData(CompoundTag nbt, boolean validateRules)
	{
		return loadAllData(DEFAULT_KEY, nbt, validateRules);
	}
	
	public static List<ItemTradeData> loadAllData(String key, CompoundTag compound, boolean validateRules)
	{
		List<ItemTradeData> data = new ArrayList<>();
		
		ListTag listNBT = compound.getList(key, Tag.TAG_COMPOUND);
		
		for(int i = 0; i < listNBT.size(); i++)
			data.add(loadData(listNBT.getCompound(i), validateRules));
		
		return data;
	}
	
	@Override
	public void loadFromNBT(CompoundTag nbt)
	{
		
		super.loadFromNBT(nbt);
		
		if(nbt.contains("Items", Tag.TAG_LIST)) //Load Sale/Barter Items
		{
			this.items = InventoryUtil.loadAllItems("Items", nbt, 4);
		}
		else //Load from old format back when only 1 sell & barter item were allowed
		{
			this.items = new SimpleContainer(4);
			//Load the Sell Item
			if(nbt.contains("SellItem", Tag.TAG_COMPOUND))
				this.items.setItem(0, ItemStack.of(nbt.getCompound("SellItem")));
			else //Load old format from before the bartering system was made
				this.items.setItem(0, ItemStack.of(nbt));
			
			//Load the Barter Item
			if(nbt.contains("BarterItem", Tag.TAG_COMPOUND))
				this.items.setItem(2, ItemStack.of(nbt.getCompound("BarterItem")));
			else
				this.items.setItem(2, ItemStack.EMPTY);
		}
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Tag.TAG_STRING))
			this.tradeType = loadTradeType(nbt.getString("TradeDirection"));
		else
			this.tradeType = ItemTradeType.SALE;
		
		if(nbt.contains("CustomName1"))
			this.customName1 = nbt.getString("CustomName1");
		else if(nbt.contains("CustomName"))
			this.customName1 = nbt.getString("CustomName");
		else
			this.customName1 = "";
		
		if(nbt.contains("CustomName2"))
			this.customName2 = nbt.getString("CustomName2");
		else
			this.customName2 = "";

		this.resetNBTList();
		if(nbt.contains("IgnoreNBT"))
		{
			for(int i : nbt.getIntArray("IgnoreNBT"))
			{
				if(i >= 0 && i < this.enforceNBT.size())
					this.enforceNBT.set(i, false);
			}
		}
	}
	
	public static ItemTradeType loadTradeType(String name)
	{
		ItemTradeType value = ItemTradeType.SALE;
		try {
			value = ItemTradeType.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeDirection.");
		}
		return value;
	}
	
	public static List<ItemTradeData> listOfSize(int tradeCount, boolean validateRules)
	{
		List<ItemTradeData> data = Lists.newArrayList();
		while(data.size() < tradeCount)
			data.add(new ItemTradeData(validateRules));
		return data;
	}
	
	
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof ItemTradeData otherItemTrade)
		{
			//Flag as compatible
			result.setCompatible();
			//Compare sell items
			result.addProductResults(ProductComparisonResult.CompareTwoItems(this.getSellItem(0), this.getSellItem(1), otherItemTrade.getSellItem(0), otherItemTrade.getSellItem(1)));
			//Compare barter items
			if(this.isBarter())
				result.addProductResults(ProductComparisonResult.CompareTwoItems(this.getBarterItem(0), this.getBarterItem(1), otherItemTrade.getBarterItem(0), otherItemTrade.getBarterItem(1)));
			//Compare prices
			if(!this.isBarter())
				result.setPriceResult(this.getCost().getRawValue() - otherTrade.getCost().getRawValue());
			//Compare types
			result.setTypeResult(this.tradeType == otherItemTrade.tradeType);
		}
		//Return the comparison results
		return result;
	}
	
	public boolean AcceptableDifferences(TradeComparisonResult result) {
		
		//Confirm the types match
		if(!result.TypeMatches() || !result.isCompatible())
			return false;
		
		//Confirm the sell item is acceptable
		if(result.getProductResultCount() < 2)
			return false;
		for(int i = 0; i < 2; ++i)
		{
			ProductComparisonResult sellResult = result.getProductResult(i);
			if(sellResult.SameProductType() && sellResult.SameProductNBT())
			{
				if(this.isSale() || this.isBarter())
				{
					//Sell product should be greater than or equal to pass
					if(sellResult.ProductQuantityDifference() > 0)
						return false;
				}
				else if(this.isPurchase())
				{
					//Purchase product should be less than or equal to pass
					if(sellResult.ProductQuantityDifference() < 0)
						return false;
				}
			}
			else //Item & tag don't match. Failure.
				return false;
		}
		//Confirm the barter item is acceptable
		if(this.isBarter())
		{
			if(result.getProductResultCount() < 4)
				return false;
			for(int i = 0; i < 2; ++i)
			{
				ProductComparisonResult barterResult = result.getProductResult(i + 2);
				if(barterResult.SameProductType() && barterResult.SameProductNBT())
				{
					//Barter product should be less than or equal to pass
					if(barterResult.ProductQuantityDifference() < 0)
						return false;
				}
				else //Item & tag don't match. Failure.
					return false;
			}
		}
		//Product is acceptable, now check the price
		if(this.isSale() && result.isPriceExpensive())
			return false;
		if(this.isPurchase() && result.isPriceCheaper())
			return false;
		
		//Products, price, and types are all acceptable.
		return true;
	}
	
	@Override
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) {
		List<Component> list = new ArrayList<>();
		//Price check
		if(!differences.PriceMatches())
		{
			//Price difference (intended - actual = difference)
			long difference = differences.priceDifference();
			if(difference < 0) //More expensive
				list.add(Component.translatable("gui.lightmanscurrency.interface.difference.expensive", MoneyUtil.getStringOfValue(-difference)).withStyle(ChatFormatting.RED));
			else //Cheaper
				list.add(Component.translatable("gui.lightmanscurrency.interface.difference.cheaper", MoneyUtil.getStringOfValue(difference)).withStyle(ChatFormatting.RED));
		}
		for(int i = 0; i < differences.getProductResultCount(); ++i)
		{
			Component slotName = Component.translatable("gui.lightmanscurrency.interface.item.difference.product." + i);
			ProductComparisonResult productCheck = differences.getProductResult(i);
			if(!productCheck.SameProductType())
				list.add(Component.translatable("gui.lightmanscurrency.interface.item.difference.itemtype", slotName).withStyle(ChatFormatting.RED));
			else
			{
				if(!productCheck.SameProductNBT()) //Don't announce changes in NBT if the item is also different
					list.add(Component.translatable("gui.lightmanscurrency.interface.item.difference.itemnbt").withStyle(ChatFormatting.RED));
				else if(!productCheck.SameProductQuantity()) //Don't announce changes in quantity if the item or nbt is also different
				{
					int quantityDifference = productCheck.ProductQuantityDifference();
					if(quantityDifference < 0) //More items
						list.add(Component.translatable("gui.lightmanscurrency.interface.item.difference.quantity.more", slotName, -quantityDifference).withStyle(ChatFormatting.RED));
					else //Less items
						list.add(Component.translatable("gui.lightmanscurrency.interface.item.difference.quantity.less", slotName, quantityDifference).withStyle(ChatFormatting.RED));	
				}
			}
		}
		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new ItemTradeButtonRenderer(this); }

	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof ItemTraderData it)
		{
			int tradeIndex = it.indexOfTrade(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				extraData.putInt("StartingSlot", -1);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
			if(this.isPurchase() && index >= 0 && index < 2)
			{
				//Set the item to the held item
				ItemStack sellItem = this.getSellItem(index);
				if(sellItem.isEmpty() && heldItem.isEmpty())
				{
					//Open Item Edit for this slot
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", index);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				else if(InventoryUtil.ItemMatches(sellItem, heldItem) && button == 1)
				{
					sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxStackSize()));
					this.setItem(sellItem, index);
				}
				else
				{
					ItemStack setItem = heldItem.copy();
					if(button == 1)
						setItem.setCount(1);
					this.setItem(setItem, index);
				}
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.sendInputInteractionMessage(tradeIndex, index, button, heldItem);
			}
			else if(this.isBarter() && index >= 0 && index < 2)
			{
				//Set the item to the held item
				ItemStack barterItem = this.getBarterItem(index);
				if(barterItem.isEmpty() && heldItem.isEmpty())
				{
					//Open Item Edit for this slot
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", index + 2);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				if(InventoryUtil.ItemMatches(barterItem, heldItem) && button == 1)
				{
					barterItem.setCount(Math.min(barterItem.getCount() + 1, barterItem.getMaxStackSize()));
					this.setItem(barterItem, index + 2);
				}
				else
				{
					ItemStack setItem = heldItem.copy();
					if(button == 1)
						setItem.setCount(1);
					this.setItem(setItem, index + 2);
				}
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.sendInputInteractionMessage(tradeIndex, index, button, heldItem);
			}
		}
	}
	
	/**
	 * Code used for item slot interactions. Works on the assumption that we're in the Item Edit Tab
	 */
	public void onSlotInteraction(int index, ItemStack heldItem, int button) {
		if(index < 2)
		{
			//Set the item to the held item
			ItemStack sellItem = this.getSellItem(index);
			if(sellItem.isEmpty() && heldItem.isEmpty())
				return;
			if(InventoryUtil.ItemMatches(sellItem, heldItem) && button == 1)
			{
				sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxStackSize()));
				this.setItem(sellItem, index);
			}
			else
			{
				ItemStack setItem = heldItem.copy();
				if(button == 1)
					setItem.setCount(1);
				this.setItem(setItem, index);
			}
		}
		if(this.isBarter() && index >= 2 && index < 4)
		{
			//Set the item to the held item
			ItemStack barterItem = this.getItem(index);
			if(barterItem.isEmpty() && heldItem.isEmpty())
				return;
			if(InventoryUtil.ItemMatches(barterItem, heldItem) && button == 1)
			{
				barterItem.setCount(Math.min(barterItem.getCount() + 1, barterItem.getMaxStackSize()));
				this.setItem(barterItem, index);
			}
			else
			{
				ItemStack setItem = heldItem.copy();
				if(button == 1)
					setItem.setCount(1);
				this.setItem(setItem, index);
			}
		}
	}

	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof ItemTraderData it)
		{
			int tradeIndex = it.indexOfTrade(this);
			if(tradeIndex < 0)
				return;
			if((this.isSale() || this.isBarter()) && index >= 0 && index < 2)
			{
				//Set the item to the held item
				ItemStack sellItem = this.getSellItem(index);
				if(sellItem.isEmpty() && heldItem.isEmpty())
				{
					//Open Item Edit for this slot
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", index);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				if(InventoryUtil.ItemMatches(sellItem, heldItem) && button == 1)
				{
					sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxStackSize()));
					this.setItem(sellItem, index);
				}
				else
				{
					ItemStack setItem = heldItem.copy();
					if(button == 1)
						setItem.setCount(1);
					this.setItem(setItem, index);
				}
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.sendOutputInteractionMessage(tradeIndex, index, button, heldItem);
			}
			else if(this.isPurchase())
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				extraData.putInt("StartingSlot", -1);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
		}
	}

	@Override
	//Open the trade edit tab if you click on a non-interaction slot.
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof ItemTraderData it)
		{
			int tradeIndex = it.indexOfTrade(this);
			if(tradeIndex < 0)
				return;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}

	@Override
	protected void collectRelevantInventorySlots(TradeContext context, NonNullList<Slot> slots, List<Integer> results) {
		if(this.isPurchase())
		{
			//Highlight purchase items
			context.hightlightItems(
					Lists.newArrayList(
							this.getItemRequirement(0),
							this.getItemRequirement(1)),
					slots, results);
		}
		else if(this.isBarter())
		{
			//Hightlight barter items
			context.hightlightItems(
					Lists.newArrayList(
							this.getItemRequirement(2),
							this.getItemRequirement(3)),
					slots, results);
		}
	}

	@Override
	public boolean isMoneyRelevant() { return !this.isBarter(); }

}

package io.github.lightman314.lightmanscurrency.common.traders.item.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart2;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.filter.FilterAPI;
import io.github.lightman314.lightmanscurrency.api.filter.IItemTradeFilter;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.RuleSupportingTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.tabs.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.CodecData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemStorageSource;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemTradeData extends RuleSupportingTradeData {

    public static final Codec<ItemTradeData> CODEC = LCRegistries.ITEM_TRADE.byNameCodec()
            .dispatch(ItemTradeData::getType,ItemTradeType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemTradeData> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.ITEM_TRADE_KEY)
            .dispatch(ItemTradeData::getType,ItemTradeType::streamCodec);

    public static final ItemTradeType<ItemTradeData> DEFAULT_TYPE = new Type();

    private static final MapCodec<ItemTradeData> DEFAULT_MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            itemFields())
            .and(ruleFields(builder))
            .apply(builder,ItemTradeData::new));

    protected static <T extends ItemTradeData> App<RecordCodecBuilder.Mu<T>,CodecData> itemFields() {
        return CodecData.CODEC.forGetter(ItemTradeData::getCodecData);
    }

    private static final StreamCodec<RegistryFriendlyByteBuf,ItemTradeData> DEFAULT_STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
            CodecData.STREAM_CODEC,ItemTradeData::getCodecData,
            ItemTradeData::new);

    protected static <T extends ItemTradeData> SPart2<RegistryFriendlyByteBuf,T,CodecData,MoneyValue> itemStreamFields() {
        return SPart2.of(baseStreamFields(),CodecData.STREAM_CODEC,ItemTradeData::getCodecData);
    }

	public static TradeDirection getNextInCycle(TradeDirection direction)
	{
		int index = direction.index + 1;
		if(index > TradeDirection.BARTER.index)
			index = 0;
		return TradeDirection.fromIndex(index);
	}

    public final CodecData getCodecData() { return new CodecData(this.items,this.enforceNBT,this.tradeType,this.customName1,this.customName2); }

	public ItemTradeData(boolean validateRules) { super(validateRules); this.resetNBTList(); }
    protected ItemTradeData(CodecData data, MoneyValue cost) { this(data,cost,new HashMap<>()); }
    protected ItemTradeData(CodecData data, MoneyValue cost, Map<TradeRuleType<?>,TradeRule> rules)
    {
        super(rules,cost);
        for(int i = 0; i < 4; ++i)
        {
            this.items.set(i,data.items().get(i));
            this.enforceNBT.set(i,data.enforceNBT().get(i));
        }
        this.tradeType = data.type();
        this.customName1 = data.customName1();
        this.customName2 = data.customName2();
    }

    public ItemTradeType<?> getType() { return DEFAULT_TYPE; }

	ItemTradeRestriction restriction = ItemTradeRestriction.NONE;
	List<ItemStack> items = NonNullList.withSize(4,ItemStack.EMPTY);
	final List<Boolean> enforceNBT = Lists.newArrayList(true, true, true, true);
	private void resetNBTList() { for(int i = 0; i < 4; ++i) this.enforceNBT.set(i, true); }
	TradeDirection tradeType = TradeDirection.SALE;
	String customName1 = "";
	String customName2 = "";

	public ItemStack getSellItem(int index)
	{
		if(index >= 0 && index < 2)
			return this.getRestriction().modifySellItem(this.items.get(index).copy(), this.getCustomName(index), this, index);
		return ItemStack.EMPTY;
	}

	public List<ItemStack> getRandomSellItems(ItemTraderData trader) { return this.getRestriction().getRandomSellItems(trader, this); }

	public ItemStack getBarterItem(int index)
	{
		if(index >= 0 && index < 2)
			return this.items.get(index + 2).copy();
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

	public final ItemStack getActualItem(int index)
	{
		if(index >= 0 && index < 4)
			return this.items.get(index);
		return ItemStack.EMPTY;
	}
	
	public void setItem(ItemStack itemStack, int index)
	{
		if(index >= 0 && index < 4)
		{
			if(index < 2)
			{
                if(FilterAPI.itemHasFilter(itemStack) && this.allowFilters())
                    this.items.set(index,itemStack); //Always allow filter items unless explicitly blocked
				else if(this.getRestriction().allowSellItem(itemStack) || itemStack.isEmpty())
					this.items.set(index, this.getRestriction().filterSellItem(itemStack).copy());
			}
			else
				this.items.set(index, itemStack.copy());
            this.setChanged();
		}
		else
			LightmansCurrency.LogError("Cannot define the item trades item at index " + index + ". Must be between 0-3!");
	}

    public boolean isNotStrict(int slot) { return !this.getEnforceNBT(slot) || FilterAPI.itemHasFilter(this.getActualItem(slot)); }

	public boolean alwaysEnforcesNBT(int slot) { return this.getRestriction().alwaysEnforceNBT(slot); }

	public boolean getEnforceNBT(int slot) {
		if(slot >= 0 && slot < 4)
			return this.enforceNBT.get(slot) || this.alwaysEnforcesNBT(slot);
		return true;
	}

	public void setEnforceNBT(int slot, boolean newValue) {
		if(slot >= 0 && slot < 4)
        {
            newValue = newValue || this.alwaysEnforcesNBT(slot);
            if(this.enforceNBT.get(slot) != newValue)
            {
                this.enforceNBT.set(slot, newValue);
                this.setChanged();
            }
        }
	}

	public ItemRequirement getItemRequirement(int slot) {
		if(slot >= 0 && slot < 4)
		{
            //Custom ItemTradeFilter items
            ItemStack rawItem = this.getActualItem(slot);
            IItemTradeFilter filter = FilterAPI.tryGetFilter(rawItem);
            if(filter != null && (slot >= 2 || this.allowFilters()))
            {
                Predicate<ItemStack> predicate = filter.getFilter(rawItem);
                if(predicate != null)
                {
                    if(slot < 2)
                        predicate = this.getRestriction().modifyFilter(predicate);
                    return ItemRequirement.fromFilter(rawItem,predicate);
                }
            }
            ItemStack item = this.getItem(slot);
			if(this.getEnforceNBT(slot))
				return ItemRequirement.of(item);
			else
				return ItemRequirement.ofItemNoNBT(item);
		}
		return ItemRequirement.getNull();
	}

	public boolean allowItemInStorage(ItemStack item) {
        int max = this.isBarter() ? 4 : 2;
		for(int i = 0; i < max; ++i)
		{
			if(this.getItemRequirement(i).test(item))
				return true;
		}
		return this.getRestriction().allowExtraItemInStorage(item);
	}

	public boolean shouldStorageItemBeSaved(ItemStack item) {
		if((this.isSale() || this.isBarter()) && this.isValid())
		{
			//Only loop through sale items, as purchase items don't matter as far as storage is concerned.
			for(int i = 0; i < 2; ++i)
			{
				if(this.isNotStrict(i) && this.getItemRequirement(i).test(item))
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
        this.setChanged();
	}
	
	@Override
	public TradeDirection getTradeDirection() { return this.tradeType; }
    @Override
    public void setTradeDirection(TradeDirection direction) {
        if(direction == TradeDirection.OTHER)
            return;
        this.setTradeType(direction);
    }
	
	public boolean isSale() { return this.tradeType == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeType == TradeDirection.PURCHASE; }
	public boolean isBarter() { return this.tradeType == TradeDirection.BARTER; }
	
	public void setTradeType(TradeDirection tradeDirection) {
        if(tradeDirection != this.tradeType)
        {
            this.tradeType = tradeDirection;
            this.setChanged();
        }
    }
	
	public ItemTradeRestriction getRestriction() { return this.restriction; }

    public final boolean allowFilters() { return this.getRestriction().allowFilters(); }
	
	public void setRestriction(ItemTradeRestriction restriction) { this.restriction = restriction; }
	
	@Override
	public boolean isValid()
	{
		if(this.tradeType == TradeDirection.BARTER)
			return this.sellItemsDefined() && this.barterItemsDefined();
		return super.isValid() && this.sellItemsDefined();
	}
	
	public boolean sellItemsDefined() {
		return this.getItemRequirement(0).isValid() || this.getItemRequirement(1).isValid();
	}
	
	public boolean barterItemsDefined() {
		return this.getItemRequirement(2).isValid() || this.getItemRequirement(3).isValid();
	}
	
	public boolean hasStock(TraderData trader)
	{
		return this.stockCount(trader) > 0;
	}

    protected static TraderItemStorage getStorage(TraderData trader)
    {
        for(TraderNode node : trader.getNodeIterable())
        {
            if(node instanceof IItemStorageSource source)
                return source.getStorage();
        }
        return new TraderItemStorage();
    }

	public boolean hasSpace(ItemTraderData trader, List<ItemStack> collectableItems)
	{
		return switch (this.tradeType) {
			case PURCHASE, BARTER -> getStorage(trader).canFitItems(collectableItems);
			default -> true;
		};
	}
	
	public int stockCount(TraderData trader)
	{
		if(!this.sellItemsDefined())
			return 0;

		if(trader.hasInfiniteStock())
			return 1;
		
		if(this.tradeType == TradeDirection.PURCHASE)
		{
			return this.stockCountOfCost(trader);
		}
		else if(this.tradeType == TradeDirection.SALE || this.tradeType == TradeDirection.BARTER)
		{
			return this.getRestriction().getSaleStock(getStorage(trader), this);
		}
		else //Other types are not handled yet.
			return 0;
	}

    @Override
	public int getStock(TradeContext context)
	{
		if(!this.sellItemsDefined())
            return 0;
		if(!context.hasTrader() || !(context.getTrader() instanceof ItemTraderData trader))
			return 0;
		if(trader.hasInfiniteStock())
			return 1;
		
		if(this.tradeType == TradeDirection.PURCHASE)
		{
			return this.stockCountOfCost(context);
		}
		else if(this.tradeType == TradeDirection.SALE || this.tradeType == TradeDirection.BARTER)
		{
			return this.getRestriction().getSaleStock(getStorage(trader), this);
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
		this.getRestriction().removeItemsFromStorage(storage, soldItems);
	}

    @Deprecated
	private static ItemTradeData loadOldData(CompoundTag compound, ItemTradeType<?> type, HolderLookup.Provider lookup) {
		ItemTradeData trade = type.create(true);
		trade.loadFromNBT(compound, lookup);
		return trade;
	}

    @Deprecated
	public static ItemTradeData loadOldTradeOfUnknownType(CompoundTag compoundTag, HolderLookup.Provider lookup, boolean validateRules)
	{
		ItemTradeType<?> tradeType = DEFAULT_TYPE;
		if(compoundTag.contains("Type"))
		{
			ResourceLocation type = VersionUtil.parseResource(compoundTag.getString("Type"));
			if(LCRegistries.ITEM_TRADE.containsKey(type))
                tradeType = LCRegistries.ITEM_TRADE.get(type);
		}
        return loadOldData(compoundTag,tradeType,lookup);
	}
	
	public static List<ItemTradeData> loadAllData(CompoundTag nbt, ItemTradeType<?> type, HolderLookup.Provider lookup)
	{
		return loadAllData(DEFAULT_KEY, nbt, type, lookup);
	}
	
	public static List<ItemTradeData> loadAllData(String key, CompoundTag compound, ItemTradeType<?> type, HolderLookup.Provider lookup)
	{
		List<ItemTradeData> data = new ArrayList<>();
		
		ListTag listNBT = compound.getList(key, Tag.TAG_COMPOUND);
		
		for(int i = 0; i < listNBT.size(); i++)
			data.add(loadOldData(listNBT.getCompound(i), type, lookup));
		
		return data;
	}
	
	@Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("deprecation")
	protected void loadFromNBT(CompoundTag nbt, HolderLookup.Provider lookup)
	{
		super.loadFromNBT(nbt, lookup);

		if(nbt.contains("Items", Tag.TAG_LIST)) //Load Sale/Barter Items
			this.items = InventoryUtil.buildList(InventoryUtil.loadAllItems("Items", nbt, 4, lookup));
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Tag.TAG_STRING))
			this.tradeType = loadTradeType(nbt.getString("TradeDirection"));
		else
			this.tradeType = TradeDirection.SALE;
		
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

    public void saveAdditionalSettings(SavedSettingData.MutableNodeAccess node) {}

	public void loadAdditionalSettings(SavedSettingData.NodeAccess node) {}

    public void saveAdditionalJsonData(JsonObject json, DataContext<JsonElement> context) { }

    public void loadAdditionalJsonData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException  { }

	public static TradeDirection loadTradeType(String name)
	{
		TradeDirection value = TradeDirection.SALE;
		try {
			value = TradeDirection.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeDirection.");
		}
		return value;
	}
	
	public static List<ItemTradeData> listOfSize(int tradeCount, ItemTradeType<?> type)
	{
		List<ItemTradeData> data = new ArrayList<>();
		while(data.size() < tradeCount)
			data.add(type.create(true));
		return data;
	}
	
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof ItemTradeData otherItemTrade)
		{
			//Flag as compatible
			result.setCompatible();
			//Compare sell items
			result.addProductResults(ProductComparisonResult.CompareTwoItems(this.getSellItem(0), this.getSellItem(1), otherItemTrade.getSellItem(0), otherItemTrade.getSellItem(1),this.compareNBT(otherItemTrade,0)));
			//Compare barter items
			if(this.isBarter())
				result.addProductResults(ProductComparisonResult.CompareTwoItems(this.getBarterItem(0), this.getBarterItem(1), otherItemTrade.getBarterItem(0), otherItemTrade.getBarterItem(1), this.compareNBT(otherItemTrade,2)));
			//Compare prices
			if(!this.isBarter())
				result.comparePrices(this.getCost(), otherTrade.getCost());
			//Compare types
			result.setTypeResult(this.tradeType == otherItemTrade.tradeType);
		}
		//Return the comparison results
		return result;
	}

	private boolean compareNBT(ItemTradeData otherItemTrade, int startingSlot)
	{
		for(int i = startingSlot; i < startingSlot + 2; ++i)
		{
			ItemStack true1 = this.getItem(i);
			ItemStack expected1 = otherItemTrade.getItem(i);
			//If both items are empty, ignore nbt for this slot as they are irrelevant
			if(true1.isEmpty() && expected1.isEmpty())
				continue;
			if(this.enforceNBT.get(i) || otherItemTrade.enforceNBT.get(i))
				return true;
		}
		return false;
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
					//Therefore difference < 0 is a fail
					if(sellResult.ProductQuantityDifference() < 0)
						return false;
				}
				else if(this.isPurchase())
				{
					//Purchase product should be less than or equal to pass
					//Therefore difference > 0 is a fail
					if(sellResult.ProductQuantityDifference() > 0)
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
					//Therefore difference > 0 is a fail
					if(barterResult.ProductQuantityDifference() > 0)
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
			if(differences.PriceIncompatible())
				list.add(LCText.GUI_TRADE_DIFFERENCE_MONEY_TYPE.getWithStyle(ChatFormatting.RED));
			if(this.isPurchase())
			{
				if(differences.isPriceCheaper())
					list.add(LCText.GUI_TRADE_DIFFERENCE_PURCHASE_CHEAPER.get(differences.priceDifference().getText()).withStyle(ChatFormatting.RED));
				else
					list.add(LCText.GUI_TRADE_DIFFERENCE_PURCHASE_EXPENSIVE.get(differences.priceDifference().getText()).withStyle(ChatFormatting.GOLD));
			}
			else
			{
				//Price difference
				if(differences.isPriceExpensive()) //More expensive
					list.add(LCText.GUI_TRADE_DIFFERENCE_EXPENSIVE.get(differences.priceDifference().getText()).withStyle(ChatFormatting.RED));
				else
					list.add(LCText.GUI_TRADE_DIFFERENCE_CHEAPER.get(differences.priceDifference().getText()).withStyle(ChatFormatting.GOLD));
			}
		}
		for(int i = 0; i < differences.getProductResultCount(); ++i)
		{
			int slot = this.isPurchase() ? i + 2 : i;
			ChatFormatting moreColor = slot >= 2 ? ChatFormatting.RED : ChatFormatting.GOLD;
			ChatFormatting lessColor = slot >= 2 ? ChatFormatting.GOLD : ChatFormatting.RED;
			Component slotName = slot >= 2 ? LCText.GUI_TRADE_DIFFERENCE_ITEM_PURCHASING.get() : LCText.GUI_TRADE_DIFFERENCE_ITEM_SELLING.get();
			ProductComparisonResult productCheck = differences.getProductResult(i);
			if(!productCheck.SameProductType())
				list.add(LCText.GUI_TRADE_DIFFERENCE_ITEM_TYPE.get(slotName).withStyle(ChatFormatting.RED));
			else
			{
				if(!productCheck.SameProductNBT()) //Don't announce changes in NBT if the item is also different
					list.add(LCText.GUI_TRADE_DIFFERENCE_ITEM_NBT.get(slotName).withStyle(ChatFormatting.RED));
				else if(!productCheck.SameProductQuantity()) //Don't announce changes in quantity if the item or nbt is also different
				{
					int quantityDifference = productCheck.ProductQuantityDifference();
					if(quantityDifference > 0) //More items
						list.add(LCText.GUI_TRADE_DIFFERENCE_ITEM_QUANTITY_MORE.get(slotName, quantityDifference).withStyle(moreColor));
					else //Fewer items
						list.add(LCText.GUI_TRADE_DIFFERENCE_ITEM_QUANTITY_LESS.get(slotName, -quantityDifference).withStyle(lessColor));
				}
			}
		}
		return list;
	}

	@Override
	public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof ItemTraderData it)
		{
			int tradeIndex = it.indexOfTrade(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				tab.sendOpenTabMessage(ItemTradeEditTab.KEY,tab.builder()
						.setInt("TradeIndex", tradeIndex)
						.setInt("StartingSlot", -1));
			}
			if(this.isPurchase() && index >= 0 && index < 2)
			{
				//Set the item to the held item
				ItemStack sellItem = this.getSellItem(index);
                if(FilterAPI.itemHasFilter(heldItem) && this.allowFilters())
                    sellItem = this.getActualItem(index);
				if(data.shiftHeld() || (sellItem.isEmpty() && heldItem.isEmpty()))
				{
					//Open Item Edit for this slot if holding shift or both held & current item is empty
					tab.sendOpenTabMessage(ItemTradeEditTab.KEY,tab.builder()
							.setInt("TradeIndex", tradeIndex)
							.setInt("StartingSlot", index));
				}
				else if(InventoryUtil.ItemMatches(sellItem, heldItem) && data.mouseButton() == 1)
				{
					sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxStackSize()));
					this.setItem(sellItem, index);
				}
				else
				{
					ItemStack setItem = heldItem.copy();
					if(data.mouseButton() == 1)
						setItem.setCount(1);
					this.setItem(setItem, index);
				}
				//Only send message on client, otherwise we get an infinite loop
				if(tab.isClient())
					tab.SendInputInteractionMessage(tradeIndex, index, data, heldItem);
			}
			else if(this.isBarter() && index >= 0 && index < 2)
			{
				//Set the item to the held item
				ItemStack barterItem = this.getBarterItem(index);
				if(data.shiftHeld() || (barterItem.isEmpty() && heldItem.isEmpty()))
				{
					//Open Item Edit for this slot
					tab.sendOpenTabMessage(ItemTradeEditTab.KEY, tab.builder()
							.setInt("TradeIndex", tradeIndex)
							.setInt("StartingSlot", index + 2));
				}
				else if(InventoryUtil.ItemMatches(barterItem, heldItem) && data.mouseButton() == 1)
				{
					barterItem.setCount(Math.min(barterItem.getCount() + 1, barterItem.getMaxStackSize()));
					this.setItem(barterItem, index + 2);
				}
				else
				{
					ItemStack setItem = heldItem.copy();
					if(data.mouseButton() == 1)
						setItem.setCount(1);
					this.setItem(setItem, index + 2);
				}
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.SendInputInteractionMessage(tradeIndex, index, data, heldItem);
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
            if(FilterAPI.itemHasFilter(heldItem) && this.allowFilters()) //Compare it to the internal item if a filter is present
                sellItem = this.getActualItem(index);
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
	public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof ItemTraderData it)
		{
			int tradeIndex = it.indexOfTrade(this);
			if(tradeIndex < 0)
				return;
			if((this.isSale() || this.isBarter()) && index >= 0 && index < 2)
			{
				//Set the item to the held item
				ItemStack sellItem = this.getSellItem(index);
                if(FilterAPI.itemHasFilter(heldItem) && this.allowFilters())
                    sellItem = this.getActualItem(index);
				if(data.shiftHeld() || (sellItem.isEmpty() && heldItem.isEmpty()))
				{
					//Open Item Edit for this slot
					tab.sendOpenTabMessage(ItemTradeEditTab.KEY, tab.builder()
							.setInt("TradeIndex", tradeIndex)
							.setInt("StartingSlot", index));
				}
				else if(InventoryUtil.ItemMatches(sellItem, heldItem) && data.mouseButton() == 1)
				{
					sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxStackSize()));
					this.setItem(sellItem, index);
				}
				else
				{
					ItemStack setItem = heldItem.copy();
					if(data.mouseButton() == 1)
						setItem.setCount(1);
					this.setItem(setItem, index);
				}
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.SendOutputInteractionMessage(tradeIndex, index, data, heldItem);
			}
			else if(this.isPurchase())
			{
				tab.sendOpenTabMessage(ItemTradeEditTab.KEY, tab.builder()
						.setInt("TradeIndex", tradeIndex)
						.setInt("StartingSlot", -1));
			}
		}
	}

	@Override
	//Open the trade edit tab if you click on a non-interaction slot.
	public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof ItemTraderData it)
		{
			int tradeIndex = it.indexOfTrade(this);
			if(tradeIndex < 0)
				return;
			tab.sendOpenTabMessage(ItemTradeEditTab.KEY, tab.builder().setInt("TradeIndex", tradeIndex));
		}
	}

	@Override
	protected void collectRelevantInventorySlots(TradeContext context, List<Slot> slots, List<Integer> results) {
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

    private static class Type extends ItemTradeType<ItemTradeData>
    {
        @Override
        public ItemTradeData create(boolean validateTrades) { return new ItemTradeData(validateTrades); }
        @Override
        public MapCodec<ItemTradeData> codec() { return DEFAULT_MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ItemTradeData> streamCodec() { return DEFAULT_STREAM_CODEC; }

        @Override
        public ItemTradeData changeType(ItemTradeData other) {
            return new ItemTradeData(other.getCodecData(),other.getCost(),other.getRuleMap());
        }
    }

}

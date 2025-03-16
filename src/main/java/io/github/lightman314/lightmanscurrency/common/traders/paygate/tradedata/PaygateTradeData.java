package io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.common.text.TimeUnitTextEntry;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.client.PaygateTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.DemandPricing;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PaygateTradeData extends TradeData {

	public PaygateTradeData() { super(true); }
	
	int duration = PaygateTraderData.DURATION_MIN;
	public int getDuration() { return Math.max(this.duration, PaygateTraderData.DURATION_MIN); }
	public void setDuration(int duration) { this.duration = Math.max(duration, PaygateTraderData.DURATION_MIN); }

	int level = 15;
	public int getRedstoneLevel() { return this.level; }
	public void setRedstoneLevel(int level) { this.level = Math.clamp(level,1,15); }

	public String description = "";
	public String getDescription() { return this.description; }
	public void setDescription(String description) { this.description = description; }

	Item ticketItem = Items.AIR;
	long ticketID = Long.MIN_VALUE;
	int ticketColor = 0xFFFFFF;
	public int getTicketColor() { return this.ticketColor; }
	public boolean isTicketTrade() { return this.ticketID >= -1; }
	public Item getTicketItem() { return this.ticketItem; }
	public long getTicketID() { return this.ticketID; }
	public void setTicket(ItemStack ticket) {
		TicketGroupData data = TicketGroupData.getForMaster(ticket);
		if(data != null && TicketItem.isMasterTicket(ticket))
		{
			this.ticketItem = data.ticket;
			this.ticketID = TicketItem.GetTicketID(ticket);
			this.ticketColor = TicketItem.GetTicketColor(ticket);
		}
		else
		{
			this.ticketItem = Items.AIR;
			this.ticketID = Long.MIN_VALUE;
			this.ticketColor = 0xFFFFFF;
		}
		this.validateRuleStates();
	}

	@Override
	public int getStock(@Nonnull TradeContext context) { return this.isValid() ? 1 : 0; }

	@Override
	public boolean allowTradeRule(@Nonnull TradeRule rule) {
		//Block Demand Pricing trade rule from Paygates as stock is not relevant for this type of trade
		if(rule instanceof DemandPricing)
			return false;
		return super.allowTradeRule(rule);
	}

	boolean storeTicketStubs = false;
	public boolean shouldStoreTicketStubs() { return this.storeTicketStubs; }
	public void setStoreTicketStubs(boolean value) { this.storeTicketStubs = value; }
	public ItemStack getTicketStub() {
		TicketGroupData data = TicketGroupData.getForTicket(new ItemStack(this.ticketItem));
		if(data != null)
			return new ItemStack(data.ticketStub);
		return ItemStack.EMPTY;
	}

	@Override
	public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

	public boolean canAfford(TradeContext context) {
		if(this.isTicketTrade())
			return context.hasTicket(this.ticketID) || context.hasPass(this.ticketID);
		else
			return context.hasFunds(this.cost);
	}
	
	@Override
	public boolean isValid() {
		return this.getDuration() >= PaygateTraderData.DURATION_MIN && (this.isTicketTrade() || super.isValid());
	}
	
	public static void saveAllData(CompoundTag nbt, List<PaygateTradeData> data, @Nonnull HolderLookup.Provider lookup)
	{
		saveAllData(nbt, data, DEFAULT_KEY,lookup);
	}
	
	public static void saveAllData(CompoundTag nbt, List<PaygateTradeData> data, String key, @Nonnull HolderLookup.Provider lookup)
	{
		ListTag listNBT = new ListTag();

		for (PaygateTradeData datum : data)
			listNBT.add(datum.getAsNBT(lookup));
		
		if(!listNBT.isEmpty())
			nbt.put(key, listNBT);
	}
	
	public static PaygateTradeData loadData(CompoundTag nbt, @Nonnull HolderLookup.Provider lookup) {
		PaygateTradeData trade = new PaygateTradeData();
		trade.loadFromNBT(nbt,lookup);
		return trade;
	}
	
	public static List<PaygateTradeData> loadAllData(CompoundTag nbt, @Nonnull HolderLookup.Provider lookup)
	{
		return loadAllData(DEFAULT_KEY, nbt,lookup);
	}
	
	public static List<PaygateTradeData> loadAllData(String key, CompoundTag nbt, @Nonnull HolderLookup.Provider lookup)
	{
		ListTag listNBT = nbt.getList(key, Tag.TAG_COMPOUND);
		
		List<PaygateTradeData> data = listOfSize(listNBT.size());
		
		for(int i = 0; i < listNBT.size(); i++)
			data.get(i).loadFromNBT(listNBT.getCompound(i),lookup);
		
		return data;
	}
	
	public static List<PaygateTradeData> listOfSize(int tradeCount)
	{
		List<PaygateTradeData> data = Lists.newArrayList();
		while(data.size() < tradeCount)
			data.add(new PaygateTradeData());
		return data;
	}
	
	@Override
	public CompoundTag getAsNBT(@Nonnull HolderLookup.Provider lookup) {
		CompoundTag compound = super.getAsNBT(lookup);
		
		compound.putInt("Duration", this.getDuration());
		compound.putInt("Level",this.level);
		compound.putString("Description",this.description);

		if(this.ticketID >= -1)
		{
			compound.putString("TicketItem", BuiltInRegistries.ITEM.getKey(this.ticketItem).toString());
			compound.putLong("TicketID", this.ticketID);
			compound.putInt("TicketColor", this.ticketColor);
			compound.putBoolean("StoreTicketStubs", this.storeTicketStubs);
		}
		
		return compound;
	}
	
	@Override
	protected void loadFromNBT(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		super.loadFromNBT(compound,lookup);
		
		this.duration = compound.getInt("Duration");

		if(compound.contains("Level"))
			this.level = compound.getInt("Level");

		if(compound.contains("Description"))
			this.description = compound.getString("Description");

		if(compound.contains("TicketID"))
		{
			this.ticketID = compound.getLong("TicketID");
			if(compound.contains("TicketItem"))
				this.ticketItem = BuiltInRegistries.ITEM.get(VersionUtil.parseResource(compound.getString("TicketItem")));
			else
				this.ticketItem = ModItems.TICKET.get();
		}
		else
		{
			this.ticketID = Long.MIN_VALUE;
			this.ticketItem = Items.AIR;
		}

		if(compound.contains("TicketColor"))
			this.ticketColor = compound.getInt("TicketColor");
		else if(this.ticketID >= -1)
			this.ticketColor = TicketItem.GetDefaultTicketColor(this.ticketID);

		if(compound.contains("StoreTicketStubs"))
			this.storeTicketStubs = compound.getBoolean("StoreTicketStubs");
		else
			this.storeTicketStubs = false;
		
	}
	
	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		LightmansCurrency.LogWarning("Attempting to compare paygate trades, but paygate trades do not support this interaction.");
		return new TradeComparisonResult();
	}

	@Override
	public boolean AcceptableDifferences(TradeComparisonResult result) {
		LightmansCurrency.LogWarning("Attempting to determine if the paygate trades differences are acceptable, but paygate trades do not support this interaction.");
		return false;
	}

	@Override
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) {
		LightmansCurrency.LogWarning("Attempting to get warnings for different paygate trades, but paygate trades do not support this interaction.");
		return Lists.newArrayList();
	}
	
	public static MutableComponent formatDurationShort(int duration) { 
		
		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		MutableComponent result = EasyText.empty();
		if(hours > 0)
			result.append(formatUnitShort(hours, LCText.TIME_UNIT_HOUR));
		if(minutes > 0)
			result.append(formatUnitShort(minutes, LCText.TIME_UNIT_MINUTE));
		if(seconds > 0)
			result.append(formatUnitShort(seconds, LCText.TIME_UNIT_SECOND));
		if(ticks > 0 || result.getString().isBlank())
			result.append(formatUnitShort(ticks, LCText.TIME_UNIT_TICK));
		return result;
	}
	
	public static MutableComponent formatDurationDisplay(int duration) { 
		
		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		if(hours > 0)
			return formatUnitShort(hours,LCText.TIME_UNIT_HOUR);
		if(minutes > 0)
			return formatUnitShort(minutes,LCText.TIME_UNIT_MINUTE);
		if(seconds > 0)
			return formatUnitShort(seconds,LCText.TIME_UNIT_SECOND);
		return formatUnitShort(ticks,LCText.TIME_UNIT_TICK);
	}
	
	public static MutableComponent formatDuration(int duration) { 
		
		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		MutableComponent result = EasyText.empty();
		boolean addSpacer = false;
		if(hours > 0)
		{
			appendUnit(result, false, hours, LCText.TIME_UNIT_HOUR);
			addSpacer = true;

		}	
		if(minutes > 0)
		{
			appendUnit(result, addSpacer, minutes, LCText.TIME_UNIT_MINUTE);
			addSpacer = true;
		}
		if(seconds > 0)
		{
			appendUnit(result, addSpacer, seconds, LCText.TIME_UNIT_SECOND);
			addSpacer = true;
		}
		if(ticks > 0)
		{
			appendUnit(result, addSpacer, ticks, LCText.TIME_UNIT_TICK);
			//addSpacer = true;
		}
		return result;
	}

	private static void appendUnit(@Nonnull MutableComponent result, boolean addSpacer, int count, @Nonnull TimeUnitTextEntry entry)
	{
		if(addSpacer)
			result.append(EasyText.literal(" "));
		result.append(EasyText.literal(String.valueOf(count)));
		if(count > 1)
			result.append(entry.pluralText.get());
		else
			result.append(entry.fullText.get());
	}

	@Nonnull
	private static MutableComponent formatUnitShort(int count, @Nonnull TimeUnitTextEntry entry) { return EasyText.literal(String.valueOf(count)).append(entry.shortText.get()); }

	@Nonnull
    @Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new PaygateTradeButtonRenderer(this); }

	@Override
	public void OnInputDisplayInteraction(@Nonnull BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			int tradeIndex = paygate.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(TicketItem.isMasterTicket(heldItem))
			{
				this.setTicket(heldItem);
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.SendInputInteractionMessage(tradeIndex, 0, data, heldItem);
			}
			else
			{
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, tab.builder().setInt("TradeIndex", tradeIndex).setBoolean("PriceEdit",true));
			}
		}
	}

	@Override
	public void OnOutputDisplayInteraction(@Nonnull BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			int tradeIndex = paygate.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, tab.builder().setInt("TradeIndex", tradeIndex).setBoolean("PriceEdit",false));
		}
	}

	@Override
	public void OnInteraction(@Nonnull BasicTradeEditTab tab, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		
		if(tab.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			int tradeIndex = paygate.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, tab.builder().setInt("TradeIndex", tradeIndex));
		}
		
	}

	@Override
	public boolean isMoneyRelevant() { return !this.isTicketTrade(); }

}

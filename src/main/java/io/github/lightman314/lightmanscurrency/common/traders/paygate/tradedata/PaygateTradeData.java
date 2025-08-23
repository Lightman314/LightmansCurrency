package io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.IDescriptionTrade;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
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
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PaygateTradeData extends TradeData implements IDirectionalSettingsObject, IDescriptionTrade {

	public PaygateTradeData() {
		super(true);
		for(Direction side : Direction.values())
			this.outputSettings.setState(side,DirectionalSettingsState.OUTPUT);
	}

	private PaygateTraderData parent = null;
	public void setParent(PaygateTraderData parent) { this.parent = parent; }
	
	int duration = PaygateTraderData.DURATION_MIN;
	public int getDuration() { return Math.max(this.duration, PaygateTraderData.DURATION_MIN); }
	public void setDuration(int duration) { this.duration = Math.max(duration, PaygateTraderData.DURATION_MIN); }

	int level = 15;
	public int getRedstoneLevel() { return this.level; }
	public void setRedstoneLevel(int level) { this.level = Math.clamp(level,1,15); }

	private String description = "";
	@Override
	public String getDescription() { return this.description; }
	public void setDescription(String description) { this.description = description; }

	private String tooltip = "";
	@Override
	public String getTooltip() { return this.tooltip; }
	public void setTooltip(String tooltip) { this.tooltip = tooltip; }
	public List<Component> getDescriptionTooltip()
	{
		if(!this.tooltip.isBlank())
		{
			List<Component> lines = new ArrayList<>();
			for(String line : this.tooltip.split("\\\\n"))
				lines.add(EasyText.literal(line));
			return lines;
		}
		return ImmutableList.of(EasyText.literal(this.description));
	}

	DirectionalSettings outputSettings = new DirectionalSettings(this);
	public DirectionalSettings getOutputSides() { return this.outputSettings; }
	@Override
	public boolean allowInputs() { return false; }
	@Nullable
	@Override
	public Block getDisplayBlock() { return ModBlocks.PAYGATE.get(); }
	@Nullable
	@Override
	public ResourceLocation getVariant() { return this.parent == null ? null : this.parent.getTraderBlockVariant(); }
	@Override
	public DirectionalSettingsState getSidedState(Direction side) { return this.outputSettings.getState(side); }

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
	public int getStock(TradeContext context) { return this.isValid() ? 1 : 0; }

	@Override
	public boolean allowTradeRule(TradeRule rule) {
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
			return context.hasTicket(this.ticketID) || context.hasInfinitePass(this.ticketID);
		else
			return context.hasFunds(this.cost);
	}
	
	@Override
	public boolean isValid() {
		return this.getDuration() >= PaygateTraderData.DURATION_MIN && (this.isTicketTrade() || super.isValid()) && this.hasOutputSide();
	}
	
	public static void saveAllData(CompoundTag nbt, List<PaygateTradeData> data, HolderLookup.Provider lookup)
	{
		saveAllData(nbt, data, DEFAULT_KEY,lookup);
	}
	
	public static void saveAllData(CompoundTag nbt, List<PaygateTradeData> data, String key, HolderLookup.Provider lookup)
	{
		ListTag listNBT = new ListTag();

		for (PaygateTradeData datum : data)
			listNBT.add(datum.getAsNBT(lookup));
		
		if(!listNBT.isEmpty())
			nbt.put(key, listNBT);
	}
	
	public static PaygateTradeData loadData(CompoundTag nbt, HolderLookup.Provider lookup) {
		PaygateTradeData trade = new PaygateTradeData();
		trade.loadFromNBT(nbt,lookup);
		return trade;
	}
	
	public static List<PaygateTradeData> loadAllData(CompoundTag nbt, HolderLookup.Provider lookup)
	{
		return loadAllData(DEFAULT_KEY, nbt,lookup);
	}
	
	public static List<PaygateTradeData> loadAllData(String key, CompoundTag nbt, HolderLookup.Provider lookup)
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

	public static void setupParents(List<PaygateTradeData> trades, PaygateTraderData parent)
	{
		for(PaygateTradeData trade : trades)
			trade.setParent(parent);
	}
	
	@Override
	public CompoundTag getAsNBT(HolderLookup.Provider lookup) {
		CompoundTag compound = super.getAsNBT(lookup);
		
		compound.putInt("Duration", this.getDuration());
		compound.putInt("Level",this.level);
		compound.putString("Description",this.description);
		compound.putString("Tooltip",this.tooltip);
		this.outputSettings.save(compound,"OutputSides");

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
	protected void loadFromNBT(CompoundTag compound, HolderLookup.Provider lookup) {
		super.loadFromNBT(compound,lookup);
		
		this.duration = compound.getInt("Duration");

		this.outputSettings.load(compound,"OutputSides");

		if(compound.contains("Level"))
			this.level = compound.getInt("Level");

		if(compound.contains("Description"))
			this.description = compound.getString("Description");
		if(compound.contains("Tooltip"))
			this.tooltip = compound.getString("Tooltip");

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

	private static void appendUnit(MutableComponent result, boolean addSpacer, int count, TimeUnitTextEntry entry)
	{
		if(addSpacer)
			result.append(EasyText.literal(" "));
		result.append(EasyText.literal(String.valueOf(count)));
		if(count > 1)
			result.append(entry.pluralText.get());
		else
			result.append(entry.fullText.get());
	}

	
	private static MutableComponent formatUnitShort(int count, TimeUnitTextEntry entry) { return EasyText.literal(String.valueOf(count)).append(entry.shortText.get()); }

	
    @Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new PaygateTradeButtonRenderer(this); }

	@Override
	public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
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
	public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			int tradeIndex = paygate.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, tab.builder().setInt("TradeIndex", tradeIndex).setBoolean("PriceEdit",false));
		}
	}

	@Override
	public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) {
		
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

package io.github.lightman314.lightmanscurrency.common.traders.tradedata.paygate;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil.TextFormatting;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class PaygateTradeData extends TradeData {

	public PaygateTradeData() { super(true); }
	
	int duration = PaygateTraderData.DURATION_MIN;
	public int getDuration() { return Math.max(this.duration, PaygateTraderData.DURATION_MIN); }
	public void setDuration(int duration) { this.duration = Math.max(duration, PaygateTraderData.DURATION_MIN); }
	long ticketID = Long.MIN_VALUE;
	int ticketColor = 0xFFFFFF;
	public boolean isTicketTrade() { return this.ticketID >= -1; }
	public long getTicketID() { return this.ticketID; }
	public void setTicket(ItemStack ticket) { this.ticketID = TicketItem.GetTicketID(ticket); this.ticketColor = TicketItem.GetTicketColor(ticket); }

	@Override
	public TradeDirection getTradeDirection() { return TradeDirection.SALE; }
	
	public boolean canAfford(TradeContext context) {
		if(this.isTicketTrade())
			return context.hasTicket(this.ticketID);
		else
			return context.hasFunds(this.cost);
	}
	
	@Override
	public boolean isValid() {
		return this.getDuration() >= PaygateTraderData.DURATION_MIN && (this.isTicketTrade() || super.isValid());
	}
	
	public static CompoundTag saveAllData(CompoundTag nbt, List<PaygateTradeData> data)
	{
		return saveAllData(nbt, data, DEFAULT_KEY);
	}
	
	public static CompoundTag saveAllData(CompoundTag nbt, List<PaygateTradeData> data, String key)
	{
		ListTag listNBT = new ListTag();

		for (PaygateTradeData datum : data)
			listNBT.add(datum.getAsNBT());
		
		if(listNBT.size() > 0)
			nbt.put(key, listNBT);
		
		return nbt;
	}
	
	public static PaygateTradeData loadData(CompoundTag nbt) {
		PaygateTradeData trade = new PaygateTradeData();
		trade.loadFromNBT(nbt);
		return trade;
	}
	
	public static List<PaygateTradeData> loadAllData(CompoundTag nbt)
	{
		return loadAllData(DEFAULT_KEY, nbt);
	}
	
	public static List<PaygateTradeData> loadAllData(String key, CompoundTag nbt)
	{
		ListTag listNBT = nbt.getList(key, Tag.TAG_COMPOUND);
		
		List<PaygateTradeData> data = listOfSize(listNBT.size());
		
		for(int i = 0; i < listNBT.size(); i++)
		{
			//CompoundNBT compoundNBT = listNBT.getCompound(i);
			data.get(i).loadFromNBT(listNBT.getCompound(i));
		}
		
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
	public CompoundTag getAsNBT() {
		CompoundTag compound = super.getAsNBT();
		
		compound.putInt("Duration", this.getDuration());
		if(this.ticketID >= -1)
			compound.putLong("TicketID", this.ticketID);
		
		return compound;
	}
	
	@Override
	protected void loadFromNBT(CompoundTag compound) {
		super.loadFromNBT(compound);
		
		this.duration = compound.getInt("Duration");

		if(compound.contains("TicketID"))
			this.ticketID = compound.getLong("TicketID");
		else if(compound.contains("Ticket"))
			this.ticketID = TicketSaveData.getConvertedID(compound.getUUID("Ticket"));
		else
			this.ticketID = Long.MIN_VALUE;
		
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
	
	@Override
	public int tradeButtonWidth(TradeContext context) { return 94; }

	@Override
	public DisplayData inputDisplayArea(TradeContext context) {
		return new DisplayData(1, 1, 34, 16);
	}

	@Override
	public DisplayData outputDisplayArea(TradeContext context) {
		return new DisplayData(58, 1, 34, 16);
	}
	
	@Override
	public Pair<Integer,Integer> arrowPosition(TradeContext context) {
		return Pair.of(36, 1);
	}

	@Override
	public Pair<Integer,Integer> alertPosition(TradeContext context) {
		return Pair.of(36, 1);
	}

	@Override
	public List<DisplayEntry> getInputDisplays(TradeContext context) {
		if(this.isTicketTrade())
			return Lists.newArrayList(DisplayEntry.of(TicketItem.CreateTicket(this.ticketID, this.ticketColor), 1, Lists.newArrayList(Component.translatable("tooltip.lightmanscurrency.ticket.id", this.ticketID))));
		else
			return Lists.newArrayList(DisplayEntry.of(this.getCost(context), context.isStorageMode ? Lists.newArrayList(Component.translatable("tooltip.lightmanscurrency.trader.price_edit")) : null));
	}

	@Override
	public List<DisplayEntry> getOutputDisplays(TradeContext context) {
		return Lists.newArrayList(DisplayEntry.of(formatDurationDisplay(this.duration), TextFormatting.create(), Lists.newArrayList(formatDuration(this.getDuration()))));
	}
	
	public static MutableComponent formatDurationShort(int duration) { 
		
		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		MutableComponent result = Component.empty();
		if(hours > 0)
			result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.hours.short", hours));
		if(minutes > 0)
			result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.short", minutes));
		if(seconds > 0)
			result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.short", seconds));
		if(ticks > 0 || result.getString().isBlank())
			result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.short", ticks));
		return result;
	}
	
	public static MutableComponent formatDurationDisplay(int duration) { 
		
		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		if(hours > 0)
			return Component.translatable("tooltip.lightmanscurrency.paygate.duration.hours.short", hours);
		if(minutes > 0)
			return Component.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.short", minutes);
		if(seconds > 0)
			return Component.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.short", seconds);
		return Component.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.short", ticks);
	}
	
	public static MutableComponent formatDuration(int duration) { 
		
		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		MutableComponent result = Component.empty();
		boolean addSpacer = false;
		if(hours > 0)
		{
			if(addSpacer)
				result.append(Component.literal(" "));
			addSpacer = true;
			if(hours > 1)
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.hours", hours));
			else
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.hours.singular", hours));
		}	
		if(minutes > 0)
		{
			if(addSpacer)
				result.append(Component.literal(" "));
			addSpacer = true;
			if(minutes > 1)
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.minutes", minutes));
			else
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.singular", minutes));
		}
		if(seconds > 0)
		{
			if(addSpacer)
				result.append(Component.literal(" "));
			addSpacer = true;
			if(seconds > 1)
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.seconds", seconds));
			else
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.singular", seconds));
		}
		if(ticks > 0)
		{
			if(addSpacer)
				result.append(Component.literal(" "));
			addSpacer = true;
			if(ticks > 1)
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.ticks", ticks));
			else
				result.append(Component.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.singular", ticks));
		}
		return result;
	}

	@Override
	protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
		if(context.hasTrader() && context.getTrader() instanceof PaygateTraderData)
		{
			PaygateTraderData paygate = (PaygateTraderData)context.getTrader();
			//Check whether the paygate is currently active
			if(paygate.isActive())
				alerts.add(AlertData.warn(Component.translatable("tooltip.lightmanscurrency.paygate.active")));
			//Check whether they can afford the costs
			if(!this.canAfford(context))
				alerts.add(AlertData.warn(Component.translatable("tooltip.lightmanscurrency.cannotafford")));
		}
	}

	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			int tradeIndex = paygate.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(heldItem.getItem() == ModItems.TICKET_MASTER.get())
			{
				this.setTicket(heldItem);
				//Only send message on client, otherwise we get an infinite loop
				if(tab.menu.isClient())
					tab.sendInputInteractionMessage(tradeIndex, 0, button, heldItem);
			}
			else
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
		}
	}

	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof PaygateTraderData)
		{
			PaygateTraderData paygate = (PaygateTraderData)tab.menu.getTrader();
			int tradeIndex = paygate.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}

	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) {
		
		if(tab.menu.getTrader() instanceof PaygateTraderData)
		{
			PaygateTraderData paygate = (PaygateTraderData)tab.menu.getTrader();
			int tradeIndex = paygate.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
		
	}
	
}

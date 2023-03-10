package io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.client.PaygateTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PaygateTradeData extends TradeData {

	public PaygateTradeData() { super(true); }

	int duration = PaygateTraderData.DURATION_MIN;
	public int getDuration() { return Math.max(this.duration, PaygateTraderData.DURATION_MIN); }
	public void setDuration(int duration) { this.duration = Math.max(duration, PaygateTraderData.DURATION_MIN); }
	long ticketID = Long.MIN_VALUE;
	int ticketColor = 0xFFFFFF;
	public int getTicketColor() { return this.ticketColor; }
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

	public static void saveAllData(CompoundNBT nbt, List<PaygateTradeData> data)
	{
		saveAllData(nbt, data, DEFAULT_KEY);
	}

	public static void saveAllData(CompoundNBT nbt, List<PaygateTradeData> data, String key)
	{
		ListNBT listNBT = new ListNBT();

		for (PaygateTradeData datu : data)
			listNBT.add(datu.getAsNBT());

		if(listNBT.size() > 0)
			nbt.put(key, listNBT);
	}

	public static PaygateTradeData loadData(CompoundNBT nbt) {
		PaygateTradeData trade = new PaygateTradeData();
		trade.loadFromNBT(nbt);
		return trade;
	}

	public static List<PaygateTradeData> loadAllData(CompoundNBT nbt)
	{
		return loadAllData(DEFAULT_KEY, nbt);
	}

	public static List<PaygateTradeData> loadAllData(String key, CompoundNBT nbt)
	{
		ListNBT listNBT = nbt.getList(key, Constants.NBT.TAG_COMPOUND);

		List<PaygateTradeData> data = listOfSize(listNBT.size());

		for(int i = 0; i < listNBT.size(); i++)
			data.get(i).loadFromNBT(listNBT.getCompound(i));

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
	public CompoundNBT getAsNBT() {
		CompoundNBT compound = super.getAsNBT();

		compound.putInt("Duration", this.getDuration());
		if(this.ticketID >= -1)
		{
			compound.putLong("TicketID", this.ticketID);
			compound.putInt("TicketColor", this.ticketColor);
		}


		return compound;
	}

	@Override
	protected void loadFromNBT(CompoundNBT compound) {
		super.loadFromNBT(compound);

		this.duration = compound.getInt("Duration");

		if(compound.contains("TicketID"))
			this.ticketID = compound.getLong("TicketID");
		if(compound.contains("TicketColor"))
			this.ticketColor = compound.getInt("TicketColor");
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
	public List<ITextComponent> GetDifferenceWarnings(TradeComparisonResult differences) {
		LightmansCurrency.LogWarning("Attempting to get warnings for different paygate trades, but paygate trades do not support this interaction.");
		return Lists.newArrayList();
	}

	public static IFormattableTextComponent formatDurationShort(int duration) {

		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		IFormattableTextComponent result = EasyText.empty();
		if(hours > 0)
			result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.hours.short", hours));
		if(minutes > 0)
			result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.short", minutes));
		if(seconds > 0)
			result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.short", seconds));
		if(ticks > 0 || result.getString().isEmpty())
			result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.short", ticks));
		return result;
	}

	public static IFormattableTextComponent formatDurationDisplay(int duration) {

		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		if(hours > 0)
			return EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.hours.short", hours);
		if(minutes > 0)
			return EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.short", minutes);
		if(seconds > 0)
			return EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.short", seconds);
		return EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.short", ticks);
	}

	public static IFormattableTextComponent formatDuration(int duration) {

		int ticks = duration % 20;
		int seconds = (duration / 20) % 60;
		int minutes = (duration / 1200 ) % 60;
		int hours = (duration / 72000);
		IFormattableTextComponent result = EasyText.empty();
		boolean addSpacer = false;
		if(hours > 0)
		{
			if(addSpacer)
				result.append(EasyText.literal(" "));
			addSpacer = true;
			if(hours > 1)
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.hours", hours));
			else
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.hours.singular", hours));
		}
		if(minutes > 0)
		{
			if(addSpacer)
				result.append(EasyText.literal(" "));
			addSpacer = true;
			if(minutes > 1)
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.minutes", minutes));
			else
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.singular", minutes));
		}
		if(seconds > 0)
		{
			if(addSpacer)
				result.append(EasyText.literal(" "));
			addSpacer = true;
			if(seconds > 1)
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.seconds", seconds));
			else
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.singular", seconds));
		}
		if(ticks > 0)
		{
			if(addSpacer)
				result.append(EasyText.literal(" "));
			if(ticks > 1)
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.ticks", ticks));
			else
				result.append(EasyText.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.singular", ticks));
		}
		return result;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new PaygateTradeButtonRenderer(this); }

	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof PaygateTraderData)
		{
			PaygateTraderData paygate = (PaygateTraderData)tab.menu.getTrader();
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
				CompoundNBT extraData = new CompoundNBT();
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
			CompoundNBT extraData = new CompoundNBT();
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
			CompoundNBT extraData = new CompoundNBT();
			extraData.putInt("TradeIndex", tradeIndex);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}

	}

}
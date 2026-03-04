package io.github.lightman314.lightmanscurrency.common.traders.paygate.trade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.*;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tabs.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.text.TimeUnitTextEntry;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.builtin.DemandPricing;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tickets.TicketInfo;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class PaygateTradeData extends RuleSupportingTradeData implements IDirectionalSettingsObject, IDescriptionTrade {

    public static final Codec<PaygateTradeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("duration").forGetter(PaygateTradeData::getDuration),
            Codec.INT.fieldOf("level").forGetter(PaygateTradeData::getRedstoneLevel),
            DescriptionData.CODEC.fieldOf("display").forGetter(PaygateTradeData::getDescriptionData),
            DirectionalSettings.CODEC.fieldOf("outputs").forGetter(PaygateTradeData::getOutputSides),
            TicketInfo.CODEC.optionalFieldOf("ticket").forGetter(PaygateTradeData::getTicketInfo),
            Codec.BOOL.fieldOf("store_stub").forGetter(PaygateTradeData::shouldStoreTicketStubs)
    ).and(ruleFields(builder)).apply(builder,PaygateTradeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,PaygateTradeData> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
            ByteBufCodecs.INT,PaygateTradeData::getDuration,
            ByteBufCodecs.INT,PaygateTradeData::getRedstoneLevel,
            DescriptionData.STREAM_CODEC,PaygateTradeData::getDescriptionData,
            DirectionalSettings.STREAM_CODEC,PaygateTradeData::getOutputSides,
            ByteBufCodecs.optional(TicketInfo.STREAM_CODEC),PaygateTradeData::getTicketInfo,
            ByteBufCodecs.BOOL,PaygateTradeData::shouldStoreTicketStubs,
            PaygateTradeData::new);

	public PaygateTradeData() {
		super(true);
        this.description = DescriptionData.create();
		for(Direction side : Direction.values())
			this.outputSettings.setState(side,DirectionalSettingsState.OUTPUT);
	}
    private PaygateTradeData(int duration,int level,DescriptionData description,DirectionalSettings outputs,Optional<TicketInfo> ticketInfo,boolean storeTicketStubs,MoneyValue cost) { this(duration,level,description,outputs,ticketInfo,storeTicketStubs,cost,new HashMap<>()); }
    private PaygateTradeData(int duration,int level,DescriptionData description,DirectionalSettings outputs,Optional<TicketInfo> ticketInfo,boolean storeTicketStubs,MoneyValue cost,Map<TradeRuleType<?>,TradeRule> rules)
    {
        super(rules,cost);
        this.duration = duration;
        this.level = level;
        this.description = description;
        this.outputSettings.copy(outputs);
        ticketInfo.ifPresent(info -> {
            this.ticketItem = info.ticketItem();
            this.ticketID = info.ticketID();
            this.ticketColor = info.ticketColor();
        });
    }

	private TraderData parent = null;
	public void setParent(TraderData parent) { this.parent = parent; }
	
	int duration = PaygateTraderData.DURATION_MIN;
	public int getDuration() { return Math.max(this.duration, PaygateTraderData.DURATION_MIN); }
	public void setDuration(int duration) {
        duration = Math.max(duration, PaygateTraderData.DURATION_MIN);
        if(this.duration != duration)
        {
            this.duration = duration;
            this.setChanged();
        }
    }

	int level = 15;
	public int getRedstoneLevel() { return this.level; }
	public void setRedstoneLevel(int level) { this.level = Math.clamp(level,1,15); this.setChanged(); }

    private final DescriptionData description;
    @Override
    public DescriptionData getDescriptionData() { return this.description; }
	public void setDescription(String description) { this.description.description = description; this.setChanged(); }

	public void setTooltip(String tooltip) { this.description.tooltip = tooltip; this.setChanged(); }
	public List<Component> getDescriptionTooltip() { return this.description.getTooltipLines(); }

	DirectionalSettings outputSettings = new DirectionalSettings(this).withListener(this::setChanged);
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

    private Optional<TicketInfo> getTicketInfo() {
        if(this.ticketID == Long.MIN_VALUE || this.ticketItem == Items.AIR)
            return Optional.empty();
        return Optional.of(new TicketInfo(this.ticketItem,this.ticketID,this.ticketColor));
    }

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
        this.setChanged();
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
	public void setStoreTicketStubs(boolean value) { this.storeTicketStubs = value; this.setChanged(); }
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

    @Deprecated(forRemoval = true)
	public static PaygateTradeData loadData(CompoundTag nbt, HolderLookup.Provider lookup) {
		PaygateTradeData trade = new PaygateTradeData();
		trade.loadFromNBT(nbt,lookup);
		return trade;
	}

    @Deprecated(forRemoval = true)
	public static List<PaygateTradeData> loadAllData(CompoundTag nbt, HolderLookup.Provider lookup)
	{
		return loadAllData(DEFAULT_KEY, nbt,lookup);
	}

    @Deprecated(forRemoval = true)
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

	public static void setupParents(List<PaygateTradeData> trades, TraderData parent)
	{
		for(PaygateTradeData trade : trades)
			trade.setParent(parent);
	}
	
	@Override
	protected void loadFromNBT(CompoundTag compound, HolderLookup.Provider lookup) {
		super.loadFromNBT(compound,lookup);
		
		this.duration = compound.getInt("Duration");

		this.outputSettings.loadOldData(compound,"OutputSides");

		if(compound.contains("Level"))
			this.level = compound.getInt("Level");

		if(compound.contains("Description"))
			this.description.description = compound.getString("Description");
		if(compound.contains("Tooltip"))
			this.description.tooltip = compound.getString("Tooltip");

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
				tab.sendOpenTabMessage(PaygateTradeEditTab.KEY, tab.builder().setInt("TradeIndex", tradeIndex).setBoolean("PriceEdit",true));
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
			tab.sendOpenTabMessage(PaygateTradeEditTab.KEY, tab.builder().setInt("TradeIndex", tradeIndex).setBoolean("PriceEdit",false));
		}
	}

	@Override
	public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) {
		
		if(tab.menu.getTrader() instanceof PaygateTraderData paygate)
		{
			int tradeIndex = paygate.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			tab.sendOpenTabMessage(PaygateTradeEditTab.KEY, tab.builder().setInt("TradeIndex", tradeIndex));
		}
		
	}

	@Override
	public boolean isMoneyRelevant() { return !this.isTicketTrade(); }

}

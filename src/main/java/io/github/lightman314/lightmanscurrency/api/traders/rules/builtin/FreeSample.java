package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import java.util.*;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.*;
import io.github.lightman314.lightmanscurrency.api.traders.rules.data.PlayerMemory;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class FreeSample extends PriceTweakingTradeRule implements ICopySupportingRule, IPersistentRule {
	
	public static final TradeRuleType<FreeSample> TYPE = new Type();

    private static final MapCodec<FreeSample> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("limit").forGetter(FreeSample::getLimit),
            Codec.LONG.fieldOf("timeLimit").forGetter(FreeSample::getTimeLimit),
            PlayerMemory.CODEC.fieldOf("memory").forGetter(FreeSample::getMemory),
            Codec.INT.fieldOf("total").forGetter(FreeSample::getSampleCount),
            baseFields()
    ).apply(builder,FreeSample::new));

	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = MathUtil.clamp(newLimit,1,100); }

	private long timeLimit = 0;
	private boolean enforceTimeLimit() { return this.timeLimit > 0; }
	public long getTimeLimit() { return this.timeLimit; }
	public void setTimeLimit(long timeLimit) { this.timeLimit = timeLimit; }

	private final PlayerMemory memory;
    public PlayerMemory getMemory() { return this.memory; }

	private int totalCount = 0;
	public int getSampleCount() { return this.totalCount; }
	
	private FreeSample() { this.memory = new PlayerMemory(); }
    private FreeSample(int limit,long timeLimit,PlayerMemory memory,int totalCount,boolean active)
    {
        super(active);
        this.limit = limit;
        this.timeLimit = timeLimit;
        this.memory = memory;
        this.totalCount = totalCount;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, Player player) {
        builder.setInt("limit",this.limit)
                .setLong("timeLimit",this.timeLimit)
                .setMap("memory",this.memory.encode(source.get(),player))
                .setInt("totalCount",this.totalCount);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.limit = data.getInt("limit");
        this.timeLimit = data.getLong("timeLimit");
        this.memory.copyFrom(PlayerMemory.decode(data.getMap("memory")));
        this.totalCount = data.getInt("totalCount");
    }

    @Override
	protected boolean canActivate(@Nullable ITradeRuleHost host) {
		if(host instanceof TradeData trade && trade.getTradeDirection() != TradeDirection.SALE)
			return false;
		return super.canActivate(host);
	}
	
	@Override
	public IconData getIcon() { return IconUtil.ICON_FREE_SAMPLE; }

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.giveDiscount(event))
		{
			if(this.limit > 1)
			{
				event.addHelpful(LCText.TRADE_RULE_FREE_SAMPLE_INFO_MULTI.get(this.limit));
				int count = this.getFreeSampleCount(event);
				if(count > 0)
					event.addHelpful(LCText.TRADE_RULE_FREE_SAMPLE_INFO_USED.get(count,this.limit));
			}
			else
				event.addHelpful(LCText.TRADE_RULE_FREE_SAMPLE_INFO_SINGLE.get());
		}
		else
		{
			int count = this.getFreeSampleCount(event);
			if(count > 0)
				event.addNeutral(LCText.TRADE_RULE_FREE_SAMPLE_INFO_USED.get(count,this.limit));
		}
		if(this.enforceTimeLimit())
			event.addNeutral(LCText.TRADE_RULE_FREE_SAMPLE_INFO_TIMED.get(new TimeUtil.TimeData(this.getTimeLimit()).getString()));
	}
	
	@Override
	public void tradeCost(TradeCostEvent event) {
		if(this.giveDiscount(event))
			event.makeFree();
	}

	@Override
	public boolean afterTrade(PostTradeEvent event) {
		if(this.giveDiscount(event))
		{
			this.addToMemory(event.getPlayerReference().id);
			this.memory.clearExpiredData(this.timeLimit);
			return true;
		}
        else
            return this.memory.clearExpiredData(this.timeLimit);
	}
	
	private boolean giveDiscount(TradeEvent event) {
		return event.hasPlayerReference() && this.giveDiscount(event.getPlayerReference().id) && event.getTrade().getTradeDirection() == TradeDirection.SALE;
	}
	
	private void addToMemory(UUID playerID) {
        this.memory.addEntry(playerID);
		this.totalCount++;
	}
	
	public boolean giveDiscount(UUID playerID) { return this.getFreeSampleCount(playerID) < this.limit; }

	private int getFreeSampleCount(TradeEvent event) { return getFreeSampleCount(event.getPlayerReference().id); }
	private int getFreeSampleCount(UUID playerID)
	{
		return this.memory.getCount(playerID,this.timeLimit);
	}

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        JsonObject json = new JsonObject();
        json.addProperty("limit", this.limit);
        if(this.enforceTimeLimit())
            json.addProperty("forget_time", this.timeLimit);
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Limit"))
            this.limit = GsonHelper.getAsInt(json,"Limit");
        else
            this.limit = GsonHelper.getAsInt(json,"limit");
        if(json.has("ForgetTime"))
            this.timeLimit = GsonHelper.getAsLong(json,"ForgetTime");
        else
            this.timeLimit = GsonHelper.getAsLong(json,"forget_time",0);
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("count",this.totalCount);
        tag.put("memory",PlayerMemory.CODEC.encodeStart(context.ops(),this.memory).getOrThrow());
        return tag;
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        this.loadMemory(tag,context);
    }

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("ForgetTime"))
			this.timeLimit = compound.getLong("ForgetTime");
		this.loadMemory(compound,DataContext.createNBT(lookup));
	}

    @SuppressWarnings("deprecation")
	private void loadMemory(CompoundTag compound,DataContext<Tag> context)
	{
		if(compound.contains("Total"))
			this.totalCount = compound.getInt("Total");
        else if(compound.contains("total"))
            this.totalCount = compound.getInt("total");
		if(compound.contains("Memory"))
            this.memory.loadOldData(compound);
        else if(compound.contains("memory"))
            this.memory.copyFrom(PlayerMemory.CODEC.decode(context.ops(),compound.get("memory")).getOrThrow().getFirst());
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("limit",this.limit);
		node.setLongValue("time_limit",this.timeLimit);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.limit = node.getIntValue("limit");
		this.timeLimit = node.getLongValue("time_limit");
	}

	@Override
	public void resetToDefaultState() {
		this.limit = 1;
		this.timeLimit = 0;
		this.memory.clear();
	}
	
	@Override
	protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
		if(updateInfo.contains("Limit"))
			this.limit = updateInfo.getInt("Limit");
		else if(updateInfo.contains("TimeLimit"))
			this.timeLimit = updateInfo.getLong("TimeLimit");
		else if(updateInfo.contains("ClearMemory"))
			this.memory.clear();
	}

    private static class Type extends TradeRuleType<FreeSample>
    {
        @Override
        public FreeSample create() { return new FreeSample(); }
        @Override
        public MapCodec<FreeSample> mapCodec() { return MAP_CODEC; }
    }
	
}

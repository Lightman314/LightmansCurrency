package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.PriceTweakingTradeRule;
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

public class PriceFluctuation extends PriceTweakingTradeRule implements ICopySupportingRule, IPersistentRule {

	public static final TradeRuleType<PriceFluctuation> TYPE = new Type();

    private static final MapCodec<PriceFluctuation> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.LONG.fieldOf("duration").forGetter(PriceFluctuation::getDuration),
            Codec.INT.fieldOf("fluctuation").forGetter(PriceFluctuation::getFluctuation),
            baseFields()
    ).apply(builder,PriceFluctuation::new));

	long duration = TimeUtil.DURATION_DAY;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, TimeUtil.DURATION_MINUTE, Long.MAX_VALUE); }
	int fluctuation = 10;
	public int getFluctuation() { return this.fluctuation; }
	public void setFluctuation(int fluctuation) { this.fluctuation = MathUtil.clamp(fluctuation, 1, 100); }
	
	public PriceFluctuation() { }
    private PriceFluctuation(long duration,int fluctuation,boolean active) {
        super(active);
        this.duration = duration;
        this.fluctuation = fluctuation;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, Player player) {
        builder.setLong("duration",this.duration)
                .setInt("fluctuation",this.fluctuation);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.duration = data.getLong("duration");
        this.fluctuation = data.getInt("fluctuation");
    }

    @Override
	public IconData getIcon() { return IconUtil.ICON_PRICE_FLUCTUATION; }

	private static final List<Long> debuggedSeeds = new ArrayList<>();
	private static final List<Long> debuggedTraderFactors = new ArrayList<>();
	
	private static void debugTraderFactor(long factor, long traderID, int tradeIndex)
	{
		if(debuggedTraderFactors.contains(factor))
			return;
		LightmansCurrency.LogDebug("Trader Seed Factor for trader with id '" + traderID + "' and trade index '" + tradeIndex + "' is " + factor);
		debuggedTraderFactors.add(factor);
	}
	
	private static void debugFlux(long seed, int maxFlux, int flux)
	{
		if(debuggedSeeds.contains(seed))
			return;
		LightmansCurrency.LogDebug("Price Fluctuation for trade with seed '" + (seed) + "' and max fluctuation of " + maxFlux + "% is " + flux + "%");
		debuggedSeeds.add(seed);
	}
	
	private long getTraderSeedFactor(TradeCostEvent event) {
		long traderID = event.getTrader().getID();
		int tradeIndex = event.getTradeIndex();
		long factor = ((traderID + 1) << 32) + tradeIndex;
		debugTraderFactor(factor, traderID, tradeIndex);
		return factor;
	}
	
	private int randomizePriceMultiplier(long traderSeedFactor)
	{
		//Have the seed be constant during the given duration
		long seed = TimeUtil.getCurrentTime() / this.duration;
		int fluct = new Random(seed * traderSeedFactor).nextInt(-this.fluctuation, this.fluctuation + 1);
		debugFlux(seed * traderSeedFactor, this.fluctuation, fluct);
		return fluct;
	}
	
	@Override
	public void tradeCost(TradeCostEvent event) {
		event.giveDiscount(this.randomizePriceMultiplier(this.getTraderSeedFactor(event)));
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.duration = compound.getLong("Duration");
		if(this.duration <= 0)
			this.duration = TimeUtil.DURATION_DAY;
		this.fluctuation = compound.getInt("Fluctuation");
	}

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        JsonObject json = new JsonObject();
        json.addProperty("duration",this.duration);
        json.addProperty("fluctuation",this.fluctuation);
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Duration"))
            this.duration = GsonHelper.getAsLong(json,"Duration");
        else
            this.duration = GsonHelper.getAsByte(json,"duration");
        if(this.duration <= TimeUtil.DURATION_MINUTE)
            throw new JsonSyntaxException("Price Fluctuation duration cannot be less than 1 minute (" + TimeUtil.DURATION_MINUTE + "ms)!");
        if(json.has("Fluctuation"))
            this.fluctuation = GsonHelper.getAsInt(json,"Fluctuation");
        else
            this.fluctuation = GsonHelper.getAsInt(json,"fluctuation");
        if(this.fluctuation <= 0 || this.fluctuation > 100)
            throw new JsonSyntaxException("Price Fluctuation amount cannot be less than 1 or greater than 100!");
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        return IPersistentRule.super.writePersistentTag(context);
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        IPersistentRule.super.readPersistentTag(tag, context);
    }

    @Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("fluctuation",this.fluctuation);
		node.setLongValue("duration",this.duration);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.fluctuation = Math.max(1,node.getIntValue("fluctuation"));
		this.duration = Math.max(TimeUtil.DURATION_MINUTE,node.getLongValue("duration"));
	}

	@Override
	public void resetToDefaultState() {
		this.fluctuation = 10;
		this.duration = TimeUtil.DURATION_DAY;
	}
	
	@Override
	protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
		if(updateInfo.contains("Duration"))
			this.setDuration(updateInfo.getLong("Duration"));
		if(updateInfo.contains("Fluctuation"))
			this.setFluctuation(updateInfo.getInt("Fluctuation"));
	}

    private static class Type extends TradeRuleType<PriceFluctuation>
    {
        @Override
        public PriceFluctuation create() { return new PriceFluctuation(); }
        @Override
        public MapCodec<PriceFluctuation> mapCodec() { return MAP_CODEC; }
    }

}

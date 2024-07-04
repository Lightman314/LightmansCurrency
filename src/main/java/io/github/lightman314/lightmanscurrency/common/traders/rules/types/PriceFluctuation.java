package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PriceFluctuationTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class PriceFluctuation extends PriceTweakingTradeRule {

	public static final TradeRuleType<PriceFluctuation> TYPE = new TradeRuleType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "price_fluctuation"),PriceFluctuation::new);
	
	long duration = TimeUtil.DURATION_DAY;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, 1000, Long.MAX_VALUE); }
	int fluctuation = 10;
	public int getFluctuation() { return this.fluctuation; }
	public void setFluctuation(int fluctuation) { this.fluctuation = MathUtil.clamp(fluctuation, 1, Integer.MAX_VALUE); }
	
	public PriceFluctuation() { super(TYPE); }
	
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
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		compound.putLong("Duration", this.duration);
		compound.putInt("Fluctuation", this.fluctuation);
		
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		this.duration = compound.getLong("Duration");
		if(this.duration <= 0)
			this.duration = TimeUtil.DURATION_DAY;
		
		this.fluctuation = compound.getInt("Fluctuation");
		
	}
	
	@Override
	public JsonObject saveToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
		
		json.addProperty("Duration", this.duration);
		json.addProperty("Fluctuation", this.fluctuation);
		
		return json;
	}
	
	@Override
	public void loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
		if(json.has("Duration"))
			this.duration = json.get("Duration").getAsLong();
		if(json.has("Fluctuation"))
			this.fluctuation = json.get("Fluctuation").getAsInt();
	}
	
	@Override
	public CompoundTag savePersistentData(@Nonnull HolderLookup.Provider lookup) { return null; }
	@Override
	public void loadPersistentData(@Nonnull CompoundTag data, @Nonnull HolderLookup.Provider lookup) {}
	
	@Override
	protected void handleUpdateMessage(@Nonnull LazyPacketData updateInfo) {
		if(updateInfo.contains("Duration"))
			this.setDuration(updateInfo.getLong("Duration"));
		if(updateInfo.contains("Fluctuation"))
			this.setFluctuation(updateInfo.getInt("Fluctuation"));
	}

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PriceFluctuationTab(parent); }

}

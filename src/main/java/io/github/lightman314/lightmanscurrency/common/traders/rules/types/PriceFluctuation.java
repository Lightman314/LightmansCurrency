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
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PriceFluctuation extends PriceTweakingTradeRule {

	public static final TradeRuleType<PriceFluctuation> TYPE = new TradeRuleType<>(VersionUtil.lcResource("price_fluctuation"),PriceFluctuation::new);
	
	long duration = TimeUtil.DURATION_DAY;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, TimeUtil.DURATION_MINUTE, Long.MAX_VALUE); }
	int fluctuation = 10;
	public int getFluctuation() { return this.fluctuation; }
	public void setFluctuation(int fluctuation) { this.fluctuation = MathUtil.clamp(fluctuation, 1, 100); }
	
	public PriceFluctuation() { super(TYPE); }

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
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		compound.putLong("Duration", this.duration);
		compound.putInt("Fluctuation", this.fluctuation);
		
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		this.duration = compound.getLong("Duration");
		if(this.duration <= 0)
			this.duration = TimeUtil.DURATION_DAY;
		
		this.fluctuation = compound.getInt("Fluctuation");
		
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json, HolderLookup.Provider lookup) {
		
		json.addProperty("Duration", this.duration);
		json.addProperty("Fluctuation", this.fluctuation);
		
		return json;
	}
	
	@Override
	public void loadFromJson(JsonObject json, HolderLookup.Provider lookup) {
		if(json.has("Duration"))
			this.duration = GsonHelper.getAsLong(json,"Duration");
		if(json.has("Fluctuation"))
			this.fluctuation = GsonHelper.getAsInt(json,"Fluctuation");
	}
	
	@Override
	public CompoundTag savePersistentData(HolderLookup.Provider lookup) { return null; }
	@Override
	public void loadPersistentData(CompoundTag data, HolderLookup.Provider lookup) {}
	
	@Override
	protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
		if(updateInfo.contains("Duration"))
			this.setDuration(updateInfo.getLong("Duration"));
		if(updateInfo.contains("Fluctuation"))
			this.setFluctuation(updateInfo.getInt("Fluctuation"));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PriceFluctuationTab(parent); }

}

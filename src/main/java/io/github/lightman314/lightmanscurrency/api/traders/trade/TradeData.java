package io.github.lightman314.lightmanscurrency.api.traders.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TradeData implements ITradeRuleHost {

	public static final String DEFAULT_KEY = "Trades";

	@Nonnull
	protected MoneyValue cost = MoneyValue.empty();

	List<TradeRule> rules = new ArrayList<>();

	public abstract TradeDirection getTradeDirection();

	public boolean validCost() { return this.getCost().isValidPrice(); }

	public boolean isValid() { return this.validCost(); }

	@Nonnull
	public MoneyValue getCost() { return this.cost; }

	/**
	 * Standard method of obtaining the cost of a trade given a context.
	 * Will run the {@link TradeCostEvent} to calculate any price changes,
	 * and will cache the results to save framerate for client-side displays.
	 */
	@Nonnull
	public final MoneyValue getCost(@Nonnull TradeContext context) {
		if(!context.hasTrader() || !this.validCost())
			return this.getCost();
		MoneyValue baseCost = TradeRule.getBaseCost(this,context);
		TradeCostEvent event = context.getTrader().runTradeCostEvent(this, context, baseCost);
		return event.getCostResult();
	}

	/**
	 * Gets the cost after all taxes are applied.
	 * Assumes that this is a purchase trade.
	 */
	public MoneyValue getCostWithTaxes(@Nonnull TraderData trader)
	{
		MoneyValue cost = TradeRule.getBaseCost(this,TradeContext.createStorageMode(trader));
		MoneyValue taxAmount = MoneyValue.empty();
		for(ITaxCollector entry : trader.getApplicableTaxes())
			taxAmount = taxAmount.addValue(cost.percentageOfValue(entry.getTaxRate()));
		return Objects.requireNonNullElseGet(cost.addValue(taxAmount),MoneyValue::empty);
	}
	public MoneyValue getCostWithTaxes(TradeContext context)
	{
		MoneyValue cost = this.getCost(context);
		if(context.hasTrader())
		{
			TraderData trader = context.getTrader();
			MoneyValue taxAmount = MoneyValue.empty();
			for(ITaxCollector entry : trader.getApplicableTaxes())
				taxAmount = taxAmount.addValue(cost.percentageOfValue(entry.getTaxRate()));
			return cost.addValue(taxAmount);
		}
		return cost;
	}

	public void setCost(@Nonnull MoneyValue value) { this.cost = value; }

	public boolean outOfStock(@Nonnull TradeContext context) { return !this.hasStock(context); }

	public boolean hasStock(@Nonnull TradeContext context) { return this.getStock(context) > 0; }

	public abstract int getStock(@Nonnull TradeContext context);

	public final int stockCountOfCost(TraderData trader)
	{
		if(this.getCost().isFree())
			return 1;
		if(!this.getCost().isValidPrice())
			return 0;
		MoneyValue storedMoney = trader.getStoredMoney().getStoredMoney().valueOf(this.getCost().getUniqueName());
		MoneyValue price = this.getCostWithTaxes(trader);
		if(!price.isValidPrice())
			return 0;
		return (int)(storedMoney.getCoreValue() / price.getCoreValue());
	}

	public final int stockCountOfCost(TradeContext context)
	{
		if(!context.hasTrader())
			return 0;

		TraderData trader = context.getTrader();
		if(this.getCost().isFree())
			return 1;
		if(!this.getCost().isValidPrice())
			return 0;
		MoneyValue storedMoney = trader.getStoredMoney().getStoredMoney().valueOf(this.getCost().getUniqueName());
		MoneyValue price = this.getCostWithTaxes(context);
		return (int) MathUtil.SafeDivide(storedMoney.getCoreValue(), price.getCoreValue(), 1);
	}

	private final boolean validateRules;

	protected TradeData(boolean validateRules) {
		this.validateRules = validateRules;
		if(this.validateRules)
			TradeRule.ValidateTradeRuleList(this.rules, this);
	}

	public CompoundTag getAsNBT()
	{
		CompoundTag tradeNBT = new CompoundTag();
		tradeNBT.put("Price", this.cost.save());
		TradeRule.saveRules(tradeNBT, this.rules, "RuleData");

		return tradeNBT;
	}

	protected void loadFromNBT(CompoundTag nbt)
	{
		this.cost = MoneyValue.safeLoad(nbt, "Price");
		//Set whether it's free or not
		if(nbt.contains("IsFree") && nbt.getBoolean("IsFree"))
			this.cost = MoneyValue.free();

		this.rules.clear();
		if(nbt.contains("TradeRules"))
		{
			this.rules = TradeRule.loadRules(nbt, "TradeRules", this);
			for(TradeRule r : this.rules) r.setActive(true);
		}
		else
			this.rules = TradeRule.loadRules(nbt, "RuleData", this);

		if(this.validateRules)
			TradeRule.ValidateTradeRuleList(this.rules, this);

	}

	@Override
	public final boolean isTrader() { return false; }

	@Override
	public final boolean isTrade() { return true; }

	public void beforeTrade(PreTradeEvent event) {
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.beforeTrade(event);
		}
	}

	public void tradeCost(TradeCostEvent event)
	{
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.tradeCost(event);
		}
	}

	public void afterTrade(PostTradeEvent event) {
		for(TradeRule rule : this.rules)
		{
			if(rule.isActive())
				rule.afterTrade(event);
		}
	}

	@Nonnull
	@Override
	public List<TradeRule> getRules() { return new ArrayList<>(this.rules); }

	@Override
	public void markTradeRulesDirty() { }

	/**
	 * Only to be used for persistent trader loading
	 */
	public void setRules(List<TradeRule> rules) { this.rules = rules; }

	/**
	 * Compares two trades to each other.<br>
	 * Should be called by the <code>True Trade</code> that would actually be executed.<br>
	 * If the <code>True Trade</code>'s {@link #getCost()}'s money value is greater than the <code>Expected Trade</code>'s {@link #getCost()} then {@link TradeComparisonResult#isPriceExpensive()} should be true, etc.
	 * @param expectedTrade The <codeExpected Trade</code> that we are checking for differences from.
	 */
	public abstract TradeComparisonResult compare(TradeData expectedTrade);

	/**
	 * Whether the results of {@link #compare(TradeData)} are acceptable and an automated trade can be carried out.
	 * Should return <code>false</code> if any changes have been made to the trade that aren't beneficial to the customer.
	 */
	public abstract boolean AcceptableDifferences(TradeComparisonResult result);

	/**
	 * Collects legible notes about the differences returned by {@link #compare(TradeData)} to be viewed by a player.<br>
	 * Used to inform them about what changes have been made so that they can make an informed decision about whether they want to accept the changes or not.
	 */
	public abstract List<Component> GetDifferenceWarnings(TradeComparisonResult differences);

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public abstract TradeRenderManager<?> getButtonRenderer();

	/**
	 * Called when an input display is clicked on in display mode.
	 * Runs on the client, but can (and should) be called on the server by running tab.sendInputInteractionMessage for consistent execution
	 *
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param index The index of the input display that was clicked.
	 * @param data A {@link TradeInteractionData} instance containing all relevant client-side data such as the mouse position/button or whether the SHIFT key was held
	 * @param heldItem The item being held by the player.
	 */
	public abstract void OnInputDisplayInteraction(@Nonnull BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem);


	/**
	 * Called when an output display is clicked on in display mode.
	 * Runs on the client, but can (and should) be called on the server by running tab.sendOutputInteractionMessage for consistent execution
	 *
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param index The index of the input display that was clicked.
	 * @param data A {@link TradeInteractionData} instance containing all relevant client-side data such as the mouse position or whether the SHIFT key was held
	 * @param heldItem The item being held by the player.
	 */
	public abstract void OnOutputDisplayInteraction(@Nonnull BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem);
	/**
	 * Called when the trade is clicked on in display mode, but the mouse wasn't over any of the input or output slots.
	 * Runs on the client, but can (and should) be called on the server by running tab.sendOtherInteractionMessage for consistent code execution.
	 *
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param data A {@link TradeInteractionData} instance containing all relevant client-side data such as the mouse position or whether the SHIFT key was held
	 * @param heldItem The item currently being held by the player.
	 */
	public abstract void OnInteraction(@Nonnull BasicTradeEditTab tab, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem);

	@Nonnull
	public final List<Integer> getRelevantInventorySlots(TradeContext context, List<Slot> slots) {
		List<Integer> results = new ArrayList<>();
		this.collectRelevantInventorySlots(context, slots, results);
		return results;
	}

	protected void collectRelevantInventorySlots(TradeContext context, List<Slot> slots, List<Integer> results) { }

}
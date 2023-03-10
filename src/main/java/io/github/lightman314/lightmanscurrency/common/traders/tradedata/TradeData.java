package io.github.lightman314.lightmanscurrency.common.traders.tradedata;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TradeData {

	public static final String DEFAULT_KEY = "Trades";

	public enum TradeDirection { SALE(0,1), PURCHASE(1,0), NONE(-1,0);
		public final int index;
		private final int nextIndex;
		public final TradeDirection next() { return fromIndex(this.nextIndex); }
		TradeDirection(int index, int nextIndex) { this.index = index; this.nextIndex = nextIndex; }
		public static TradeDirection fromIndex(int index) {
			for(TradeDirection d : TradeDirection.values())
			{
				if(d.index == index)
					return d;
			}
			return TradeDirection.SALE;
		}
	}

	protected CoinValue cost = new CoinValue();

	List<TradeRule> rules = new ArrayList<>();

	public abstract TradeDirection getTradeDirection();

	public final boolean validCost()
	{
		return this.cost.isFree() || cost.getRawValue() > 0;
	}

	public boolean isValid()
	{
		return validCost();
	}

	public CoinValue getCost()
	{
		return this.cost;
	}

	public CoinValue getCost(TradeContext context)
	{
		if(context.hasTrader() && context.hasPlayerReference())
			return context.getTrader().runTradeCostEvent(context.getPlayerReference(), this).getCostResult();
		return this.cost;
	}

	public void setCost(CoinValue value)
	{
		this.cost = value;
	}

	private final boolean validateRules;

	protected TradeData(boolean validateRules) {
		this.validateRules = validateRules;
		if(this.validateRules)
			TradeRule.ValidateTradeRuleList(this.rules, this::allowTradeRule);
	}

	public CompoundNBT getAsNBT()
	{
		CompoundNBT tradeNBT = new CompoundNBT();
		this.cost.save(tradeNBT,"Price");
		TradeRule.saveRules(tradeNBT, this.rules, "RuleData");

		return tradeNBT;
	}

	protected void loadFromNBT(CompoundNBT nbt)
	{
		cost.load(nbt, "Price");
		//Set whether it's free or not
		if(nbt.contains("IsFree"))
			this.cost.setFree(nbt.getBoolean("IsFree"));

		this.rules.clear();
		if(nbt.contains("TradeRules"))
		{
			this.rules = TradeRule.loadRules(nbt, "TradeRules");
			for(TradeRule r : this.rules) r.setActive(true);
		}
		else
			this.rules = TradeRule.loadRules(nbt, "RuleData");

		if(this.validateRules)
			TradeRule.ValidateTradeRuleList(this.rules, this::allowTradeRule);

	}

	public boolean allowTradeRule(TradeRule rule) { return true; }

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

	public List<TradeRule> getRules() { return new ArrayList<>(this.rules); }

	/**
	 * Only to be used for persistent trader loading
	 */
	public void setRules(List<TradeRule> rules) { this.rules = rules; }

	public abstract TradeComparisonResult compare(TradeData otherTrade);

	public abstract boolean AcceptableDifferences(TradeComparisonResult result);

	public abstract List<ITextComponent> GetDifferenceWarnings(TradeComparisonResult differences);

	@OnlyIn(Dist.CLIENT)
	public abstract TradeRenderManager<?> getButtonRenderer();

	/**
	 * Called when an input display is clicked on in display mode.
	 * Runs on the client, but can (and should) be called on the server by running tab.sendInputInteractionMessage for consistent execution
	 *
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
	 * @param index The index of the input display that was clicked.
	 * @param button The mouse button that was clicked.
	 * @param heldItem The item being held by the player.
	 */
	public abstract void onInputDisplayInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int index, int button, ItemStack heldItem);

	/**
	 * Called when an output display is clicked on in display mode.
	 * Runs on the client, but can (and should) be called on the server by running tab.sendOutputInteractionMessage for consistent execution
	 *
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
	 * @param index The index of the input display that was clicked.
	 * @param button The mouse button that was clicked.
	 * @param heldItem The item being held by the player.
	 */
	public abstract void onOutputDisplayInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int index, int button, ItemStack heldItem);

	/**
	 * Called when the trade is clicked on in display mode, but the mouse wasn't over any of the input or output slots.
	 * Runs on the client, but can (and should) be called on the server by running tab.sendOtherInteractionMessage for consistent code execution.
	 *
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
	 * @param mouseX The local X position of the mouse button when it was clicked. [0,tradeButtonWidth)
	 * @param mouseY The local Y position of the mouse button when it was clicked. [0,tradeButtonHeight)
	 * @param button The mouse button that was clicked.
	 * @param heldItem The item currently being held by the player.
	 */
	public abstract void onInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem);

	@Nonnull
	public final List<Integer> getRelevantInventorySlots(TradeContext context, List<Slot> slots) {
		List<Integer> results = new ArrayList<>();
		this.collectRelevantInventorySlots(context, ImmutableList.copyOf(slots), results);
		return results;
	}

	protected void collectRelevantInventorySlots(TradeContext context, List<Slot> slots, List<Integer> results) { }

}
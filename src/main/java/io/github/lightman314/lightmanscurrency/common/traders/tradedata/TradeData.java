package io.github.lightman314.lightmanscurrency.common.traders.tradedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

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
	
	public CompoundTag getAsNBT()
	{
		CompoundTag tradeNBT = new CompoundTag();
		this.cost.save(tradeNBT,"Price");
		TradeRule.saveRules(tradeNBT, this.rules, "RuleData");
		
		return tradeNBT;
	}
	
	protected void loadFromNBT(CompoundTag nbt)
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
	
	public void addTradeRuleAlertData(List<AlertData> alerts, TradeContext context) {
		if(context.hasTrader() && context.hasPlayerReference())
		{
			PreTradeEvent pte = context.getTrader().runPreTradeEvent(context.getPlayerReference(), this);
			alerts.addAll(pte.getAlertInfo());
		}
	}
	
	public abstract TradeComparisonResult compare(TradeData otherTrade);
	
	public abstract boolean AcceptableDifferences(TradeComparisonResult result);
	
	public abstract List<Component> GetDifferenceWarnings(TradeComparisonResult differences);
	
	public static class TradeComparisonResult {
		
		//Incompatible comparison result
		private boolean compatible = false;
		/**
		 * Whether the two trades were able to be compared at all.
		 */
		public boolean isCompatible() { return this.compatible; }
		
		//Product Comparison Result
		private List<ProductComparisonResult> tradeProductResults = new ArrayList<>();
		/**
		 * Whether the product(s) for these trades matched.
		 */
		public boolean ProductMatches() { 
			for(ProductComparisonResult result : tradeProductResults) { if(!result.Identical()) return false; }
			return true;
		}
		/**
		 * Gets the product comparison results for the given product index.
		 */
		public ProductComparisonResult getProductResult(int index) {
			if(index < 0 || index >= this.tradeProductResults.size())
				return null;
			return this.tradeProductResults.get(index);
		}
		/**
		 * The number of products that were compared to each other.
		 */
		public int getProductResultCount() { return this.tradeProductResults.size(); }
		//Price Comparison Result
		private long priceChange = 0;
		/**
		 * Whether the two trades prices are the same.
		 */
		public boolean PriceMatches() { return this.priceChange == 0; }
		/**
		 * The difference in the two prices.
		 * Result is 'expected price - true price', so a value less than 0 is more expensive, while a value greater than 0 is cheaper.
		 */
		public long priceDifference() { return this.priceChange; }
		/**
		 * Whether the trade is now cheaper (difference > 0)
		 */
		public boolean isPriceCheaper() { return this.priceChange > 0; }
		/**
		 * Whether the trade is now more expensive (difference < 0)
		 */
		public boolean isPriceExpensive() { return this.priceChange < 0; }
		//Type Comparison Result
		private boolean tradeTypeMatches = true;
		/**
		 * Whether the two trades are of the same Trade Type (SALE, PURCHASE, BARTER, etc.)
		 */
		public boolean TypeMatches() { return this.tradeTypeMatches; }
		
		/**
		 * Returns if the trade comparison has all values matching.
		 */
		public boolean Identical() { return this.compatible && this.ProductMatches() && this.PriceMatches() && this.TypeMatches(); }
		
		public static class ProductComparisonResult {
			
			public boolean Identical() { return this.SameProductType() && this.SameProductNBT() && this.SameProductQuantity(); }
			
			private final boolean sameProduct;
			/**
			 * Whether the two products are the same product type (i.e. iron ingot == iron ingot; water = water, etc.)
			 */
			public boolean SameProductType() { return this.sameProduct; }
			private final boolean sameNBT;
			/**
			 * Whether the two products have the same NBT data.
			 * @return
			 */
			public boolean SameProductNBT() { return this.sameNBT; }
			private final int quantityDifference;
			/**
			 * Whether the two products have the same quantity.
			 */
			public boolean SameProductQuantity() { return this.quantityDifference == 0; }
			/**
			 * The difference between the two quantities.
			 * Calculated as 'expected quantity - true quantity', so a difference > 0 means a smaller quantity, while a difference < 0 means a larger quantity
			 */
			public int ProductQuantityDifference() { return this.quantityDifference; }
			
			private ProductComparisonResult(boolean sameProduct, boolean sameNBT, int quantityDifference) {
				this.sameProduct = sameProduct;
				this.sameNBT = sameNBT;
				this.quantityDifference = quantityDifference;
			}
			
			public static ProductComparisonResult CompareItem(ItemStack original, ItemStack query) {
				boolean isItemEqual = original.getItem() == query.getItem();
				boolean isTagEqual;
				if(original.getTag() != null)
					isTagEqual = original.getTag().equals(query.getTag());
				else
					isTagEqual = query.getTag() == null;
				int quantityDifference = original.getCount() - query.getCount();
				return new ProductComparisonResult(isItemEqual, isTagEqual, quantityDifference);
			}
			
			public static List<ProductComparisonResult> CompareTwoItems(ItemStack original1, ItemStack original2, ItemStack query1, ItemStack query2)
			{
				List<ProductComparisonResult> results = new ArrayList<>();
				boolean flipMatch = original1.getItem() == query2.getItem() && original2.getItem() == query1.getItem() && !(original1.getItem() == original2.getItem() || query1.getItem() == query2.getItem());
				if(flipMatch)
				{
					results.add(CompareItem(original1, query2));
					results.add(CompareItem(original2, query1));
				}
				else
				{
					results.add(CompareItem(original1, query1));
					results.add(CompareItem(original2, query2));
				}
				return results;
			}
			
			public static ProductComparisonResult CompareFluid(FluidStack original, FluidStack query) {
				boolean isFluidEqual = original.getFluid() == query.getFluid();
				boolean isTagEqual;
				if(original.getTag() != null)
					isTagEqual = original.getTag().equals(query.getTag());
				else
					isTagEqual = query.getTag() == null;
				int quantityDifference = original.getAmount() - query.getAmount();
				return new ProductComparisonResult(isFluidEqual, isTagEqual, quantityDifference);
			}
			
			public static ProductComparisonResult CompareEnergy(int original, int query) {
				return new ProductComparisonResult(true, true, original - query);
			}
			
		}
		
		/**
		 * Defines the product result for the next index
		 */
		public void addProductResult(ProductComparisonResult result) {
			this.tradeProductResults.add(result);
		}
		
		/**
		 * Defines the product result for the next indexes
		 */
		public void addProductResults(Collection<? extends ProductComparisonResult> results) {
			this.tradeProductResults.addAll(results);
		}
		
		/**
		 * Defines the product result for the next index
		 * @param sameProduct Whether the product type is still the same (i.e. still an iron ingot)
		 * @param sameNBT Whether the products NBT data is still the same (i.e. still has the Unbreaking III enchantment, same damage value, etc.)
		 * @param quantityDifference Difference in the products quantity. positive for selling/purchasing more, 0 for the same amount, negative for selling less
		 */
		public void addProductResult(boolean sameProduct, boolean sameNBT, int quantityDifference) {
			this.tradeProductResults.add(new ProductComparisonResult(sameProduct, sameNBT, quantityDifference));
		}
		
		/**
		 * Defines the price result
		 * Positive for costs more, 0 for costs the same, negative for costs less.
		 */
		public void setPriceResult(long priceChange) { this.priceChange = priceChange; }
		
		/**
		 * Defines the trade type result
		 */
		public void setTypeResult(boolean typeMatches) {
			this.tradeTypeMatches = typeMatches;
		}
		
		/**
		 * Defines the trade compatibility result
		 */
		public void setCompatible() { this.compatible = true; }
		
	}
	
	/**
	 * The width of the trade button.
	 */
	public abstract int tradeButtonWidth(TradeContext context);
	
	/**
	 * Whether the trade should render an arrow pointing from the inputs to the outputs.
	 */
	public boolean hasArrow(TradeContext context) { return true; }
	
	/**
	 * Where on the button the arrow should be drawn.
	 */
	public abstract Pair<Integer,Integer> arrowPosition(TradeContext context);
	
	public Pair<Integer,Integer> alertPosition(TradeContext context) { return this.arrowPosition(context); }
	
	/**
	 * The position and size of the input displays
	 */
	public abstract DisplayData inputDisplayArea(TradeContext context);
	
	/**
	 * The position and size of the output displays
	 */
	public abstract DisplayData outputDisplayArea(TradeContext context);
	
	/**
	 * The input display entries. For a sale this would be the trades price.
	 */
	public abstract List<DisplayEntry> getInputDisplays(TradeContext context);
	/**
	 * The output display entries. For a sale this would be the product being sold.
	 */
	public abstract List<DisplayEntry> getOutputDisplays(TradeContext context);
	
	/**
	 * Whether the trade has any alerts
	 */
	public boolean hasAlert(TradeContext context) { List<AlertData> alerts = this.getAlertData(context); return alerts != null && alerts.size() > 0; }
	
	/**
	 * List of alert data. Used for Out of Stock, Cannot Afford, or Trade Rule messages.
	 * Return null to display no alert.
	 */
	public final List<AlertData> getAlertData(TradeContext context) {
		if(context.isStorageMode)
			return null;
		List<AlertData> alerts = new ArrayList<>();
		this.addTradeRuleAlertData(alerts, context);
		this.getAdditionalAlertData(context, alerts);
		return alerts;
	}
	
	protected abstract void getAdditionalAlertData(TradeContext context, List<AlertData> alerts);
	
	/**
	 * Render trade-specific icons for the trade, such as the fluid traders drainable/fillable icons.
	 * @param button The button that is rendering the trade
	 * @param pose The pose stack
	 * @param mouseX The x position of the mouse.
	 * @param mouseY The y position of the mouse.
	 * @param context The context of the trade.
	 */
	@OnlyIn(Dist.CLIENT)
	public void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) { }
	
	/**
	 * Render trade-specific tooltips for the trade, such as the fluid traders drainable/fillable icons.
	 * @param context The context of the trade.
	 * @param mouseX The mouses X position relative to the left edge of the button.
	 * @param mouseY The mouses Y position relative to the top edge of the button.
	 * @return The list of tooltip text. Return null to display no tooltip.
	 */
	public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) { return null; }
	
	/**
	 * Called when an input display is clicked on in display mode.
	 * Runs on the client, but can be called on the server by running tab.sendInputInteractionMessage for consistent execution
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
	 * Runs on the client, but can be called on the server by running tab.sendOutputInteractionMessage for consistent execution
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
	 * Runs on the client, but can be called on the server by running tab.sendOtherInteractionMessage for consistent code execution.
	 * 
	 * @param tab The Trade Edit tab that is being used to display this tab.
	 * @param clientHandler The client handler that can be used to send custom client messages to the currently opened tab. Will be null on the server.
	 * @param mouseX The local X position of the mouse button when it was clicked. [0,tradeButtonWidth)
	 * @param mouseY The local Y position of the mouse button when it was clicked. [0,tradeButtonHeight)
	 * @param button The mouse button that was clicked.
	 * @param heldItem The item currently being held by the player.
	 */
	public abstract void onInteraction(BasicTradeEditTab tab, @Nullable IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem);
	
}

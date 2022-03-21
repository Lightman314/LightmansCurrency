package io.github.lightman314.lightmanscurrency.trader.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public abstract class TradeData implements ITradeRuleHandler {

	public static final String DEFAULT_KEY = "Trades";
	
	public enum TradeDirection { SALE, PURCHASE, NONE }
	
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
	
	public void setCost(CoinValue value)
	{
		this.cost = value;
	}
	
	@Deprecated
	public boolean isFree()
	{
		return this.cost.isFree();
	}
	
	@Deprecated
	public void setFree(boolean free)
	{
		this.cost.setFree(free);
	}
	
	public CompoundTag getAsNBT()
	{
		CompoundTag tradeNBT = new CompoundTag();
		this.cost.writeToNBT(tradeNBT,"Price");
		TradeRule.writeRules(tradeNBT, this.rules);
		
		return tradeNBT;
	}
	
	protected void loadFromNBT(CompoundTag nbt)
	{
		cost.readFromNBT(nbt, "Price");
		//Set whether it's free or not
		if(nbt.contains("IsFree"))
			this.cost.setFree(nbt.getBoolean("IsFree"));
		
		this.rules.clear();
		this.rules = TradeRule.readRules(nbt);
		
	}
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.rules.forEach(rule -> rule.beforeTrade(event));
	}

	public void tradeCost(TradeCostEvent event)
	{
		this.rules.forEach(rule -> rule.tradeCost(event));
	}
	
	@Override
	public void afterTrade(PostTradeEvent event) {
		this.rules.forEach(rule -> rule.afterTrade(event));
	}
	
	public List<TradeRule> getRules() { return this.rules; }
	
	public void setRules(List<TradeRule> rules) { this.rules = rules; }
	
	public void clearRules()
	{
		this.rules.clear();
	}
	
	public void markRulesDirty() { }
	
	public abstract TradeComparisonResult compare(TradeData otherTrade);
	
	public abstract boolean AcceptableDifferences(TradeComparisonResult result);
	
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
	
}

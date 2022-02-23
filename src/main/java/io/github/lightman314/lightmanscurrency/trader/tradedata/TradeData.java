package io.github.lightman314.lightmanscurrency.trader.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

public abstract class TradeData implements ITradeRuleHandler {

	public static final String DEFAULT_KEY = "Trades";
	
	public enum TradeDirection { SALE, PURCHASE, NONE }
	
	protected CoinValue cost = new CoinValue();
	//protected boolean isFree = false;
	
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
	
	public CoinValue getCost()
	{
		return this.cost;
	}
	
	public void setCost(CoinValue value)
	{
		this.cost = value;
	}
	
	public CompoundNBT getAsNBT()
	{
		CompoundNBT tradeNBT = new CompoundNBT();
		this.cost.writeToNBT(tradeNBT,"Price");
		//tradeNBT.putBoolean("IsFree", this.isFree);
		TradeRule.writeRules(tradeNBT, this.rules);
		
		return tradeNBT;
	}
	
	protected void loadFromNBT(CompoundNBT nbt)
	{
		if(nbt.contains("Price", Constants.NBT.TAG_INT))
			cost.readFromOldValue(nbt.getInt("Price"));
		else if(nbt.contains("Price"))
			cost.readFromNBT(nbt, "Price");
		//Load free status from old format
		if(nbt.contains("IsFree"))
			this.cost.setFree(nbt.getBoolean("IsFree"));//.isFree = nbt.getBoolean("IsFree");
		
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
	
	public void addRule(TradeRule newRule)
	{
		if(newRule == null)
			return;
		//Confirm a lack of duplicate rules
		for(int i = 0; i < this.rules.size(); i++)
		{
			if(newRule.type == this.rules.get(i).type)
				return;
		}
		this.rules.add(newRule);
	}
	
	public List<TradeRule> getRules() { return this.rules; }
	
	public void setRules(List<TradeRule> rules) { this.rules = rules; }
	
	public void removeRule(TradeRule rule)
	{
		if(this.rules.contains(rule))
			this.rules.remove(rule);
	}
	
	public void clearRules()
	{
		this.rules.clear();
	}
	
	public void markRulesDirty() { }
	
	public static class TradeComparisonResult {

		//Product Comparison Result
		private List<ProductComparisonResult> tradeProductResults = new ArrayList<>();
		public boolean ProductMatches() { 
			for(ProductComparisonResult result : tradeProductResults) { if(!result.Identical()) return false; }
			return true;
		}
		public ProductComparisonResult getProductResult(int index) {
			if(index < 0 || index >= this.tradeProductResults.size())
				return null;
			return this.tradeProductResults.get(index);
		}
		public int getProductResultCount() { return this.tradeProductResults.size(); }
		//Price Comparison Result
		private long priceChangeDirection = 0;
		public boolean PriceMatches() { return this.priceChangeDirection == 0; }
		public boolean isPriceCheaper() { return this.priceChangeDirection < 0; }
		public boolean isPriceExpensive() { return this.priceChangeDirection > 0; }
		//Type Comparison Result
		private boolean tradeTypeMatches = true;
		/**
		 * Whether the two trades are of the same Trade Type (SALE, PURCHASE, BARTER, etc.)
		 */
		public boolean TypeMatches() { return this.tradeTypeMatches; }

		/**
		 * Returns if the trade comparison has all values matching.
		 */
		public boolean Identical() { return this.ProductMatches() && this.PriceMatches() && this.TypeMatches(); }

		public static class ProductComparisonResult {

			public boolean Identical() { return this.SameProductType() && this.SameProductNBT() && this.SameProductQuantity(); }

			private final boolean sameProduct;
			public boolean SameProductType() { return this.sameProduct; }
			private final boolean sameNBT;
			public boolean SameProductNBT() { return this.sameNBT; }
			private final int quantityDifference;
			public boolean SameProductQuantity() { return this.quantityDifference == 0; }
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
		public void setPriceResult(long priceChangeDirection) { this.priceChangeDirection = priceChangeDirection; }

		/**
		 * Defines the trade type result
		 */
		public void setTypeResult(boolean typeMatches) {
			this.tradeTypeMatches = typeMatches;
		}

	}
	
}

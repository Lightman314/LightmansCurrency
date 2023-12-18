package io.github.lightman314.lightmanscurrency.api.traders.trade.comparison;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TradeComparisonResult {

    //Incompatible comparison result
    private boolean compatible = false;
    /**
     * Whether the two trades were able to be compared at all.
     */
    public boolean isCompatible() { return this.compatible; }

    //Product Comparison Result
    private final List<ProductComparisonResult> tradeProductResults = new ArrayList<>();
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
    private boolean priceIncompatible = false;
    private boolean priceGreater = false;
    private MoneyValue priceChange = MoneyValue.empty();
    /**
     * Whether the two trades prices are the same.
     */
    public boolean PriceMatches() { return this.priceChange.isEmpty() && !this.priceIncompatible; }
    public boolean PriceIncompatible() { return this.priceIncompatible; }
    /**
     * The difference in the two prices.
     * Result is 'expected price - true price', so a value less than 0 is more expensive, while a value greater than 0 is cheaper.
     */
    public MoneyValue priceDifference() { return this.priceChange; }
    /**
     * Whether the trade is now cheaper (difference > 0)
     */
    public boolean isPriceCheaper() { return !this.priceGreater; }
    /**
     * Whether the trade is now more expensive (difference < 0)
     */
    public boolean isPriceExpensive() { return this.priceGreater; }
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

    /**
     * Defines the product result for the next index
     */
    public void addProductResult(ProductComparisonResult result) { this.tradeProductResults.add(result); }

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
     * @param quantityDifference Difference in the products' quantity. positive for selling/purchasing more, 0 for the same amount, negative for selling less
     */
    public void addProductResult(boolean sameProduct, boolean sameNBT, int quantityDifference) {
        this.tradeProductResults.add(ProductComparisonResult.CreateRaw(sameProduct, sameNBT, quantityDifference));
    }


    public void comparePrices(@Nonnull MoneyValue currentPrice, @Nonnull MoneyValue oldPrice)
    {
        if(!currentPrice.sameType(oldPrice))
        {
            this.priceIncompatible = true;
            return;
        }
        if(currentPrice.containsValue(oldPrice))
        {
            //Either greater or equal
            if(oldPrice.containsValue(currentPrice))
                return;
            this.priceGreater = true;
            this.priceChange = currentPrice.subtractValue(oldPrice);
        }
        else //Price is smaller
        {
            this.priceGreater = false;
            this.priceChange = oldPrice.subtractValue(currentPrice);
        }
    }

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

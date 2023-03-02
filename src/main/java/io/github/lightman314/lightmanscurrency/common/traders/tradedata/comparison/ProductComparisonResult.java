package io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class ProductComparisonResult {

    public boolean Identical() { return this.SameProductType() && this.SameProductNBT() && this.SameProductQuantity(); }

    private final boolean sameProduct;
    /**
     * Whether the two products are the same product type (i.e. iron ingot == iron ingot; water = water, etc.)
     */
    public boolean SameProductType() { return this.sameProduct; }
    private final boolean sameNBT;
    /**
     * Whether the two products have the same NBT data.
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

    public ProductComparisonResult(boolean sameProduct, boolean sameNBT, int quantityDifference) {
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

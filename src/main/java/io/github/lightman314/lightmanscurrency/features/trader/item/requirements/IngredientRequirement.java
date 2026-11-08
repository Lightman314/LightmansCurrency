package io.github.lightman314.lightmanscurrency.features.trader.item.requirements;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public class IngredientRequirement extends ItemRequirement.MultiResult {

    private final Ingredient ingredient;
    public IngredientRequirement(Ingredient ingredient,int count) {
        super(count);
        this.ingredient = ingredient;
    }

    @Override
    public ItemRequirement copy() { return new IngredientRequirement(this.ingredient,this.getCount()); }

    @Override
    protected List<Holder<Item>> getPossibleResults() {
        ICustomIngredient custom = this.ingredient.getCustomIngredient();
        if(custom != null)
            return custom.items().toList();
        return this.ingredient.getValues().stream().toList();
    }

    @Override
    public boolean test(ItemResource resource) { return this.ingredient.test(resource.toStack()); }

    @Override
    public boolean canMerge(ItemRequirement other) { return other instanceof IngredientRequirement ir && ir.ingredient.equals(this.ingredient); }

}
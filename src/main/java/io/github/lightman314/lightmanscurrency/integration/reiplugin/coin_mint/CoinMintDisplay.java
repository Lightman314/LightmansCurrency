package io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint;

import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.Collections;
import java.util.Optional;

public class CoinMintDisplay extends BasicDisplay {

    public final CoinMintRecipe recipe;

    public CoinMintDisplay(CoinMintRecipe recipe) {
        super(
                //Inputs
                Collections.singletonList(EntryIngredients.ofIngredient(recipe.getIngredient())),
                //Outputs
                Collections.singletonList(EntryIngredients.of(recipe.getOutputItem())),
                //ID
                Optional.of(recipe.getId()));
        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() { return CoinMintCategory.ID; }

}
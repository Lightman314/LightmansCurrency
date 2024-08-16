package io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint;

import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collections;
import java.util.Optional;

public class CoinMintDisplay extends BasicDisplay {

    public final CoinMintRecipe recipe;

    public CoinMintDisplay(RecipeHolder<CoinMintRecipe> holder) {
        super(
                //Inputs
                Collections.singletonList(EntryIngredients.ofIngredient(holder.value().getIngredient())),
                //Outputs
                Collections.singletonList(EntryIngredients.of(holder.value().getOutputItem())),
                //ID
                Optional.of(holder.id()));
        this.recipe = holder.value();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() { return CoinMintCategory.ID; }

}
package io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.crafting.MasterTicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;

public class TicketStationDisplay extends BasicDisplay {


    public TicketStationDisplay(TicketStationRecipe recipe) {
        super(
                //Inputs
                Lists.newArrayList(getModifierInput(recipe),
                        EntryIngredients.ofIngredient(recipe.getIngredient())),
                //Outputs
                Collections.singletonList(EntryIngredients.of(recipe.exampleResult())),
                Optional.of(recipe.getId())
        );
    }

    private static EntryIngredient getModifierInput(@Nonnull TicketStationRecipe recipe)
    {
        if(recipe instanceof MasterTicketRecipe mtr)
            return EntryIngredients.ofIngredient(Ingredient.of(Tags.Items.DYES));
        else if(recipe instanceof TicketRecipe tr)
            return EntryIngredients.ofIngredient(tr.getMasterIngredient());
        else
            return EntryIngredients.ofItemStacks(recipe.jeiModifierList());
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() { return TicketStationCategory.ID; }

}
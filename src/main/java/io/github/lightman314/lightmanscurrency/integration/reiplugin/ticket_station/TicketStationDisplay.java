package io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.crafting.MasterTicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TicketStationDisplay extends BasicDisplay {


    public TicketStationDisplay(RecipeHolder<TicketStationRecipe> holder) {
        super(
                //Inputs
                Lists.newArrayList(getModifierInput(holder.value()),
                        EntryIngredients.ofIngredient(holder.value().getIngredient())),
                //Outputs
                Collections.singletonList(EntryIngredients.of(holder.value().exampleResult())),
                Optional.of(holder.id())
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

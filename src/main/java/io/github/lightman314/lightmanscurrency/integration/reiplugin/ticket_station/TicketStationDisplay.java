package io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.crafting.MasterTicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TicketStationDisplay implements Display {

    protected List<EntryIngredient> inputs;
    protected List<EntryIngredient> outputs;
    protected Optional<ResourceLocation> location;

    public final boolean codeInputs;
    public final DurabilityData durabilityData;

    public TicketStationDisplay(TicketStationRecipe recipe) {
        this.inputs = Lists.newArrayList(getModifierInput(recipe),
                EntryIngredients.ofIngredient(recipe.getIngredient()));
        this.outputs = Collections.singletonList(EntryIngredients.of(recipe.exampleResult()));
        this.location = Optional.of(recipe.getId());
        this.codeInputs = recipe.requiredCodeInput();
        this.durabilityData = recipe.getDurabilityData();
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
    public List<EntryIngredient> getInputEntries() { return this.inputs; }

    @Override
    public List<EntryIngredient> getOutputEntries() { return this.outputs; }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() { return TicketStationCategory.ID; }

}
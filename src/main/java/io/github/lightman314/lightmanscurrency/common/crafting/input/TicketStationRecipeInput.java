package io.github.lightman314.lightmanscurrency.common.crafting.input;

import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;

public class TicketStationRecipeInput extends ListRecipeInput {

    public final TicketStationRecipe.ExtraData data;

    public TicketStationRecipeInput(IItemHandlerModifiable container, TicketStationRecipe.ExtraData data) {
        super(container);
        this.data = data;
    }

    public TicketStationRecipeInput(List<ItemStack> items, TicketStationRecipe.ExtraData data) {
        super(items);
        this.data = data;
    }
}

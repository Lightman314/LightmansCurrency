package io.github.lightman314.lightmanscurrency.common.crafting.input;

import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketStationRecipeInput extends ListRecipeInput {

    public final TicketStationRecipe.ExtraData data;

    public TicketStationRecipeInput(Container container, TicketStationRecipe.ExtraData data) {
        super(container);
        this.data = data;
    }

    public TicketStationRecipeInput(List<ItemStack> items, TicketStationRecipe.ExtraData data) {
        super(items);
        this.data = data;
    }
}

package io.github.lightman314.lightmanscurrency.common.crafting.input;

import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import net.minecraft.world.Container;

public class TicketStationRecipeInput extends SuppliedContainer {

    public final TicketStationRecipe.ExtraData data;
    public TicketStationRecipeInput(Container container, TicketStationRecipe.ExtraData data)
    {
        super(() -> container);
        this.data = data;
    }

}

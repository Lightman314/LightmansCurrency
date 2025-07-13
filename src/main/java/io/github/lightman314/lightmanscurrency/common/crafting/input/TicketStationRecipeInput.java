package io.github.lightman314.lightmanscurrency.common.crafting.input;

import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import net.minecraft.world.Container;

public class TicketStationRecipeInput extends SuppliedContainer {

    private final String code;
    public TicketStationRecipeInput(Container container, String code)
    {
        super(() -> container);
        this.code = code;
    }

    public String getCode() { return this.code; }

}

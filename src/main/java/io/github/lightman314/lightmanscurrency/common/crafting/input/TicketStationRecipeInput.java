package io.github.lightman314.lightmanscurrency.common.crafting.input;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketStationRecipeInput extends ListRecipeInput {

    private final String code;
    public String getCode() { return this.code; }

    public TicketStationRecipeInput(Container container, String code) {
        super(container);
        this.code = code;
    }

    public TicketStationRecipeInput(List<ItemStack> items, String code) {
        super(items);
        this.code = code;
    }
}

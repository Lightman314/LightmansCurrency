package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import net.minecraft.world.item.ItemStack;

public class TicketStationStorage extends NormalItemStorage {

    private final TicketStationBlockEntity blockEntity;
    public TicketStationStorage(TicketStationBlockEntity blockEntity) {
        super(2);
        this.blockEntity = blockEntity;
    }

    @Override
    protected boolean isValid(int index, ItemStack stack) {
        if(index == 0)
            return this.blockEntity.isValidModifier(stack);
        return super.isValid(index, stack);
    }
}
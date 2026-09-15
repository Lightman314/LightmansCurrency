package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot.InteractionSlotData;

import java.util.function.Consumer;

public interface IInteractionSlotProvider {

    void addInteractionSlot(Consumer<InteractionSlotData> builder);

}
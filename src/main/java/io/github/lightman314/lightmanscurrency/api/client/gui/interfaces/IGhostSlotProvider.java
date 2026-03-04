package io.github.lightman314.lightmanscurrency.api.client.gui.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.GhostSlot;

import javax.annotation.Nullable;
import java.util.List;

public interface IGhostSlotProvider {

    @Nullable
    List<GhostSlot<?>> getGhostSlots();

}

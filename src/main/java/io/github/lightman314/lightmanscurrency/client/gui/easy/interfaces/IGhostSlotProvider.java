package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;

import javax.annotation.Nullable;
import java.util.List;

public interface IGhostSlotProvider {

    @Nullable
    List<GhostSlot<?>> getGhostSlots();

}

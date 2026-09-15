package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;

import java.util.Optional;

public interface IAreaClaim {

    Optional<ScreenArea> getClaimedArea();

    static IAreaClaim of(ScreenArea area) { return () -> Optional.of(area); }

}
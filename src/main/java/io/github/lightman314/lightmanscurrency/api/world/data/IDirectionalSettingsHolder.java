package io.github.lightman314.lightmanscurrency.api.world.data;

import net.minecraft.core.Direction;

import java.util.List;

public interface IDirectionalSettingsHolder {

    IDirectionalSettingsHolder DEFAULT = new IDirectionalSettingsHolder() {};

    default List<Direction> getIgnoredSides() { return List.of(); }

    default boolean allowInputs() { return true; }
    default boolean allowOutputs() { return true; }

}
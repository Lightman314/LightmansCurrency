package io.github.lightman314.lightmanscurrency.api.misc.settings.directional;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;

import java.util.List;

public interface IDirectionalSettingsHolder {

    default List<Direction> getIgnoredSides() { return ImmutableList.of(); }

    default boolean allowInputs() { return true; }
    default boolean allowOutputs() { return true; }

}

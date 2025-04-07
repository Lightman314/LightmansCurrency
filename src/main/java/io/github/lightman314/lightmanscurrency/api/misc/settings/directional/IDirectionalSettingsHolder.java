package io.github.lightman314.lightmanscurrency.api.misc.settings.directional;

import com.google.common.collect.ImmutableList;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IDirectionalSettingsHolder {

    default List<Direction> getIgnoredSides() { return ImmutableList.of(); }

    default boolean allowInputs() { return true; }
    default boolean allowOutputs() { return true; }

}

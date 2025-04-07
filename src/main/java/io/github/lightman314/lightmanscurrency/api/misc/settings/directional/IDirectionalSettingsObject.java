package io.github.lightman314.lightmanscurrency.api.misc.settings.directional;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IDirectionalSettingsObject extends IDirectionalSettingsHolder {

    @Nullable
    Block getDisplayBlock();
    DirectionalSettingsState getSidedState(Direction side);

    default boolean allowInputSide(Direction side) { return this.getSidedState(side).allowsInputs(); }
    default boolean hasInputSide() {
        for(Direction side : Direction.values())
        {
            if(this.allowInputSide(side))
                return true;
        }
        return false;
    }

    default boolean allowOutputSide(Direction side) { return this.getSidedState(side).allowsOutputs(); }
    default boolean hasOutputSide() {
        for(Direction side : Direction.values())
        {
            if(this.allowOutputSide(side))
                return true;
        }
        return false;
    }

}

package io.github.lightman314.lightmanscurrency.api.world.data;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public interface IDirectionalSettingsObject extends IDirectionalSettingsHolder {

    @Nullable
    Block getDisplayBlock();
    DirectionalSettingsState getSidedState(Direction side);

    default boolean allowInputSide(Direction side) { return this.getSidedState(side).allowsInputs(); }
    default boolean hasInputSide() { return Direction.stream().anyMatch(this::allowInputSide); }

    default boolean allowOutputSide(Direction side) { return this.getSidedState(side).allowsOutputs(); }
    default boolean hasOutputSide() { return Direction.stream().anyMatch(this::allowInputSide); }

}
package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AccessTrackingPeripheral extends LCPeripheral {

    @Nullable
    private AccessTrackingPeripheral parent;
    public void setParent(@Nonnull AccessTrackingPeripheral parent) { this.parent = parent; }

    protected boolean childStillValid(IPeripheral child) { return true; }

    protected final boolean stillValid() {
        if(this.parent == null)
            return true;
        return this.parent.childStillValid(this);
    }

    protected final AttachedComputerSet getConnectedComputers() {
        if(this.parent != null)
            return this.parent.getConnectedComputers();
        return this.computers;
    }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) { return this == peripheral; }

}

package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AccessTrackingPeripheral extends LCPeripheral {

    List<AccessTrackingPeripheral> children = new ArrayList<>();
    @Nullable
    private AccessTrackingPeripheral parent;
    public void setParent(@Nonnull AccessTrackingPeripheral parent) { this.parent = parent; this.parent.children.add(this); }

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
    protected void onFirstAttachment() {
        super.onFirstAttachment();
        for(AccessTrackingPeripheral child : this.children)
            child.onFirstAttachment();
    }

    @Override
    protected void onLastDetachment() {
        super.onLastDetachment();
        for(AccessTrackingPeripheral child : this.children)
            child.onLastDetachment();
    }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) { return this == peripheral; }

}
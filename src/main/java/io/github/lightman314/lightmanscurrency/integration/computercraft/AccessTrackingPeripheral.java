package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class AccessTrackingPeripheral extends LCPeripheral {

    private final List<AccessTrackingPeripheral> children = new ArrayList<>();
    @Nullable
    private AccessTrackingPeripheral parent;
    public void setParent(AccessTrackingPeripheral parent) { this.parent = parent; this.parent.children.add(this); }

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

    protected final String getComputerID(IComputerAccess computer) { return "computercraft#" + computer.getID(); }
    protected final PlayerReference getFakePlayer(IComputerAccess computer) { return PlayerReference.dummy(this.getComputerID(computer)); }

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
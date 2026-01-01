package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class AccessTrackingPeripheral extends LCPeripheral {

    private final List<AccessTrackingPeripheral> children = new ArrayList<>();
    @Nullable
    private AccessTrackingPeripheral parent;
    public void setParent(AccessTrackingPeripheral parent) {
        if(this.parent != null)
        {
            LightmansCurrency.LogWarning("Attempted to attach an access tracking peripheral to a second parent!",new Throwable());
            return;
        }
        this.parent = parent;
        this.parent.children.add(this);
        //Manually trigger "onAttachment" code for new peripherals as they are attached
        this.parent.getConnectedComputers().forEach(this::onAttachment);
        //Manually trigger "onFirstAttachment" code for new peripherals as they are attached
        if(this.parent.getConnectedComputers().hasComputers())
            this.onFirstAttachment();
    }

    @Override
    protected boolean hasComputer(IComputerAccess computer) {
        boolean result = super.hasComputer(computer);
        if(!result)
        {
            if(this.parent != null)
                return this.parent.hasComputer(computer);
        }
        return result;
    }

    protected boolean childStillValid(IPeripheral child) { return true; }

    protected final boolean stillValid() {
        if(this.parent == null)
            return true;
        return this.parent.childStillValid(this);
    }

    protected final void queueEvent(String event, Object... arguments) { this.getConnectedComputers().queueEvent(event,arguments); }

    protected final void queueEvent(String event, Function<IComputerAccess,Object[]> argumentBuilder) { this.getConnectedComputers().forEach(computer -> computer.queueEvent(event,argumentBuilder.apply(computer))); }

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
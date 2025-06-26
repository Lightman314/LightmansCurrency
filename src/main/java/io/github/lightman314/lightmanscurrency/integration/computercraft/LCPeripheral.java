package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public abstract class LCPeripheral implements IPeripheral {

    protected final AttachedComputerSet computers = new AttachedComputerSet();

    protected final HolderLookup.Provider registryAccess() { return LookupHelper.getRegistryAccess(); }

    @LuaFunction
    public String[] getTypes() {
        List<String> types = new ArrayList<>();
        types.add(this.getType());
        types.addAll(this.getAdditionalTypes());
        return types.toArray(String[]::new);
    }

    protected boolean eventListener() { return false; }

    @Override
    public void attach(IComputerAccess computer) {
        boolean wasEmpty = !this.computers.hasComputers();
        this.computers.add(computer);
        if(wasEmpty && this.eventListener())
            NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void detach(IComputerAccess computer) {
        this.computers.remove(computer);
        if(this.eventListener() && !this.computers.hasComputers())
            NeoForge.EVENT_BUS.unregister(this);
    }

}

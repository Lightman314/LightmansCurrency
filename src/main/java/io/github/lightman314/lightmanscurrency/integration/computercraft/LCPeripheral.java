package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.core.computer.GuardedLuaContext;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.InventoryPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.events.PeripheralMethodsEvent;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class LCPeripheral implements IDynamicPeripheral {

    protected final AttachedComputerSet computers = new AttachedComputerSet();

    public List<String> getTypeList() {
        List<String> types = new ArrayList<>();
        types.add(this.getType());
        types.addAll(this.getAdditionalTypes());
        return types;
    }
    private String[] getTypes() { return this.getTypeList().toArray(String[]::new); }

    private boolean isTypeArg(IArguments args) throws LuaException { return this.isType(args.getString(0)); }

    public boolean isType(String queryType) { return this.getTypeList().contains(queryType); }

    protected boolean hasComputer(IComputerAccess computer)
    {
        AtomicBoolean found = new AtomicBoolean(false);
        this.computers.forEach(c -> { if(c == computer) found.set(true); });
        return found.get();
    }

    @Override
    public final void attach(IComputerAccess computer) {
        boolean wasEmpty = !this.computers.hasComputers();
        this.computers.add(computer);
        this.onAttachment(computer);
        if(wasEmpty)
            this.onFirstAttachment();
    }

    protected void onAttachment(IComputerAccess computer) { }

    protected void onFirstAttachment() { }

    @Override
    public final void detach(IComputerAccess computer) {
        this.computers.remove(computer);
        this.onDetachment(computer);
        if(!this.computers.hasComputers())
            this.onLastDetachment();
    }

    protected void onLastDetachment() { }

    protected void onDetachment(IComputerAccess computer) { }

    private List<LCPeripheralMethod> methods = null;
    protected abstract void registerMethods(LCPeripheralMethod.Registration registration);

    private void validateMethods() {
        if(this.methods == null)
        {
            //Make it map-backed so that future
            LCPeripheralMethod.Registration registration = new LCPeripheralMethod.Registration();
            //Register built-in methods
            registration.register(LCPeripheralMethod.builder("getType").simple(this::getType));
            registration.register(LCPeripheralMethod.builder("getTypes").simpleArray(this::getTypes));
            registration.register(LCPeripheralMethod.builder("isType").withArgs(this::isTypeArg));
            //Register peripheral methods
            this.registerMethods(registration);
            //Allow addons to add methods via event
            VersionUtil.postEvent(new PeripheralMethodsEvent(this,registration));
            //Collect the method list from the map values
            this.methods = registration.getResults();
        }
    }

    private List<LCPeripheralMethod> getMethods() {
        this.validateMethods();
        return this.methods.stream().filter(LCPeripheralMethod::isAccessible).toList();
    }

    @Override
    public final String[] getMethodNames() {
        return this.getMethods().stream().map(LCPeripheralMethod::getMethodName).toArray(String[]::new);
    }

    @Override
    public final MethodResult callMethod(IComputerAccess computer, ILuaContext context, int methodIndex, IArguments args) throws LuaException {
        this.validateMethods();
        LCPeripheralMethod m = this.getMethods().get(methodIndex);
        //Copy the arguments for safekeeping
        IArguments argumentCopy = new ObjectArguments(args.getAll());
        return context.executeMainThreadTask(() -> m.execute(computer,argumentCopy));
    }

    public static Object wrapContainer(IComputerAccess computer,Supplier<Boolean> canAccess,Container container,Runnable setChanged,AccessTrackingPeripheral parent) { return wrapInventory(computer,canAccess,new InvWrapper(container),setChanged,parent); }
    public static Object wrapContainer(IComputerAccess computer,Supplier<Boolean> canAccess,Supplier<Container> container,Runnable setChanged,AccessTrackingPeripheral parent) { return wrapInventory(computer,canAccess,new InvWrapper(new SuppliedContainer(container)),setChanged,parent); }
    public static Object wrapInventory(IComputerAccess computer,Supplier<Boolean> canAccess,IItemHandler handler,Runnable setChanged,AccessTrackingPeripheral parent) { return wrapInventory(computer,canAccess,() -> handler,setChanged,parent); }
    public static Object wrapInventory(IComputerAccess computer,Supplier<Boolean> canAccess,Supplier<IItemHandler> handler,Runnable setChanged, AccessTrackingPeripheral parent) {
        InventoryPeripheral p = new InventoryPeripheral(canAccess,handler,setChanged);
        p.setParent(parent);
        return p.asTable(computer);
    }

    public final Object asTable(IComputerAccess computer) { return new LCPeripheralWrapper(this,computer); }

    private record LCPeripheralWrapper(LCPeripheral peripheral,IComputerAccess computer) implements IDynamicLuaObject, GuardedLuaContext.Guard
    {
        @Override
        public boolean checkValid() { return this.peripheral.hasComputer(this.computer); }

        @Override
        public String[] getMethodNames() { return this.peripheral.getMethodNames(); }
        @Override
        public MethodResult callMethod(ILuaContext context, int index, IArguments args) throws LuaException {
            if(this.checkValid())
                return this.peripheral.callMethod(computer,context,index,args);
            else
                throw new LuaException("Peripheral is no longer mounted!");
        }
    }

}
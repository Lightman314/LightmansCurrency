package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.InventoryPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.events.PeripheralMethodsEvent;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class LCPeripheral implements IDynamicPeripheral {

    protected final AttachedComputerSet computers = new AttachedComputerSet();

    protected final HolderLookup.Provider registryAccess() { return LookupHelper.getRegistryAccess(); }

    public List<String> getTypeList() {
        List<String> types = new ArrayList<>();
        types.add(this.getType());
        types.addAll(this.getAdditionalTypes());
        return types;
    }
    private String[] getTypes() { return this.getTypeList().toArray(String[]::new); }

    private boolean isTypeArg(IArguments args) throws LuaException { return this.isType(args.getString(0)); }

    public boolean isType(String queryType) { return this.getTypeList().contains(queryType); }

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

    private List<PeripheralMethod> methods = null;
    protected abstract void registerMethods(PeripheralMethod.Registration registration);

    private void validateMethods() {
        if(this.methods == null)
        {
            //Make it map-backed so that future
            PeripheralMethod.Registration registration = new PeripheralMethod.Registration();
            //Register built-in methods
            registration.register(PeripheralMethod.builder("getType").simple(this::getType));
            registration.register(PeripheralMethod.builder("getTypes").simpleArray(this::getTypes));
            registration.register(PeripheralMethod.builder("isType").withArgs(this::isTypeArg));
            //Register peripheral methods
            this.registerMethods(registration);
            //Allow addons to add methods via event
            VersionUtil.postEvent(new PeripheralMethodsEvent(this,registration));
            //Collect the method list from the map values
            this.methods = registration.getResults();
        }
    }

    private List<PeripheralMethod> getMethods() {
        this.validateMethods();
        return this.methods.stream().filter(PeripheralMethod::isAccessible).toList();
    }

    @Override
    public final String[] getMethodNames() {
        return this.getMethods().stream().map(PeripheralMethod::getMethodName).toArray(String[]::new);
    }

    @Override
    public final MethodResult callMethod(IComputerAccess computer, ILuaContext context, int methodIndex, IArguments args) throws LuaException {
        this.validateMethods();
        PeripheralMethod m = this.getMethods().get(methodIndex);
        //Copy the arguments for safekeeping
        IArguments argumentCopy = new ObjectArguments(args.getAll());
        return context.executeMainThreadTask(() -> m.execute(computer,argumentCopy));
    }

    public static Object wrapContainer(Supplier<Boolean> canAccess,Container container) { return new InventoryPeripheral(canAccess,new InvWrapper(container)); }
    public static Object wrapContainer(Supplier<Boolean> canAccess,Supplier<Container> container) { return new InventoryPeripheral(canAccess,new InvWrapper(new SuppliedContainer(container))); }
    public static Object wrapInventory(Supplier<Boolean> canAccess,IItemHandler handler) { return new InventoryPeripheral(canAccess,handler); }
    public static Object wrapInventory(Supplier<Boolean> canAccess,Supplier<IItemHandler> handler) { return new InventoryPeripheral(canAccess,handler); }

}

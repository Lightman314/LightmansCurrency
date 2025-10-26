package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.util.ArgumentHelpers;
import dan200.computercraft.shared.peripheral.generic.GenericPeripheral;
import dan200.computercraft.shared.platform.ForgeContainerTransfer;
import dan200.computercraft.shared.util.CapabilityUtil;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InventoryPeripheral extends LCPeripheral {

    private final Supplier<Boolean> hasAccess;
    private final Supplier<IItemHandler> handler;
    public InventoryPeripheral(Supplier<Boolean> hasAccess, IItemHandler handler) { this(hasAccess,() -> handler); }
    public InventoryPeripheral(Supplier<Boolean> hasAccess, Supplier<IItemHandler> handler)
    {
        this.hasAccess = hasAccess;
        this.handler = handler;
    }

    private boolean hasHandler() { return this.handler.get() != null; }
    private IItemHandler getHandler()
    {
        IItemHandler handler = this.handler.get();
        if(handler == null)
            return new InvWrapper(new SimpleContainer(0));
        return handler;
    }

    //Peripheral Methods
    public int size() { return this.getHandler().getSlots(); }

    public Map<Integer, Map<String, ?>> list() {
        Map<Integer,Map<String,?>> result = new HashMap<>();
        IItemHandler inventory = this.getHandler();
        int size = this.getHandler().getSlots();
        for(int i = 0; i < size; ++i)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if(!stack.isEmpty())
                result.put(i + 1, VanillaDetailRegistries.ITEM_STACK.getBasicDetails(stack));
        }
        return result;
    }

    @Nullable
    public Map<String, ?> getItemDetail(IArguments args) throws LuaException {
        IItemHandler inventory = this.getHandler();
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,inventory.getSlots(),"Slot out of range (%s)");
        ItemStack stack = inventory.getStackInSlot(slot - 1);
        return stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(stack);
    }

    public long getItemLimit(IArguments args) throws LuaException {
        IItemHandler inventory = this.getHandler();
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,inventory.getSlots(),"Slot out of range (%s)");
        return inventory.getSlotLimit(slot - 1);
    }

    public int pushItems(IComputerAccess computer, IArguments args) throws LuaException {
        if(!this.hasAccess.get())
            return 0;
        //Parse Arguments
        String toName = args.getString(0);
        int fromSlot = args.getInt(1);
        Optional<Integer> limit = args.optInt(2);
        Optional<Integer> toSlot = args.optInt(3);
        //Run code
        IItemHandler from = this.getHandler();
        IPeripheral location = computer.getAvailablePeripheral(toName);
        if(location == null)
            throw new LuaException("Target '" + toName + "' does not exist");
        else {
            IItemHandler to = extractHandler(location);
            if(to == null)
                throw new LuaException("Source '" + toName + "' is not an inventory");
            else
            {
                int actualLimit = limit.orElse(Integer.MAX_VALUE);
                ArgumentHelpers.assertBetween(fromSlot,1,from.getSlots(),"From slot out of range (%s)");
                if(toSlot.isPresent())
                    ArgumentHelpers.assertBetween(toSlot.get(),1,to.getSlots(),"To slot out of range (%s)");
                return actualLimit <= 0 ? 0 : moveItem(from,fromSlot - 1,to,toSlot.orElse(0) - 1, actualLimit);
            }
        }
    }

    public int pullItems(IComputerAccess computer, IArguments args) throws LuaException {
        if(!this.hasAccess.get())
            return 0;
        //Parse Arguments
        String fromName = args.getString(0);
        int fromSlot = args.getInt(1);
        Optional<Integer> limit = args.optInt(2);
        Optional<Integer> toSlot = args.optInt(3);
        //Run InventoryMethods#pullItems
        IItemHandler to = this.getHandler();
        IPeripheral location = computer.getAvailablePeripheral(fromName);
        if(location == null)
            throw new LuaException("Source '" + fromName + "' does not exist");
        else
        {
            IItemHandler from = extractHandler(location);
            if(from == null)
                throw new LuaException("Source '" + fromName + "' is not an inventory");
            else
            {
                int actualLimit = limit.orElse(Integer.MAX_VALUE);
                ArgumentHelpers.assertBetween(fromSlot,1,from.getSlots(),"From slot out of range (%s)");
                if(toSlot.isPresent())
                    ArgumentHelpers.assertBetween(toSlot.get(),1,to.getSlots(),"To slot out of range (%s)");
                return actualLimit <= 0 ? 0 : moveItem(from,fromSlot - 1, to, toSlot.orElse(0) - 1, actualLimit);
            }
        }
    }

    @Override
    public String getType() { return "inventory"; }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if(peripheral instanceof InventoryPeripheral other)
            return this.hasHandler() && other.hasHandler() && Objects.equals(this.getHandler(),other.getHandler());
        return false;
    }

    @Nullable
    private static IItemHandler extractHandler(IPeripheral peripheral) {
        Object object = peripheral.getTarget();
        Direction var10000;
        if (peripheral instanceof GenericPeripheral sided) {
            var10000 = sided.side();
        } else {
            var10000 = null;
        }

        Direction direction = var10000;
        if (object instanceof BlockEntity blockEntity) {
            if (blockEntity.isRemoved()) {
                return null;
            }

            Level level = blockEntity.getLevel();
            if (!(level instanceof ServerLevel serverLevel)) {
                return null;
            }

            IItemHandler result = CapabilityUtil.getCapability(serverLevel, Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, direction);
            if (result != null) {
                return result;
            }
        }

        if (object instanceof IItemHandler handler) {
            return handler;
        } else if (object instanceof Container container) {
            return new InvWrapper(container);
        } else {
            return null;
        }
    }

    private static int moveItem(IItemHandler from, int fromSlot, IItemHandler to, int toSlot, int limit) {
        ForgeContainerTransfer fromWrapper = (new ForgeContainerTransfer(from)).singleSlot(fromSlot);
        ForgeContainerTransfer toWrapper = new ForgeContainerTransfer(to);
        if (toSlot >= 0) {
            toWrapper = toWrapper.singleSlot(toSlot);
        }

        return Math.max(0, fromWrapper.moveTo(toWrapper, limit));
    }

    @Override
    protected void registerMethods(PeripheralMethod.Registration registration) {
        registration.register(PeripheralMethod.builder("size").simple(this::size).build());
        registration.register(PeripheralMethod.builder("list").simple(this::list).build());
        registration.register(PeripheralMethod.builder("getItemDetail").withArgs(this::getItemDetail).build());
        registration.register(PeripheralMethod.builder("getItemLimit").withArgs(this::getItemLimit).build());
        registration.register(PeripheralMethod.builder("pushItems").withContext(this::pushItems).withAccess(this.hasAccess).build());
        registration.register(PeripheralMethod.builder("pullItems").withContext(this::pullItems).withAccess(this.hasAccess).build());
    }

}

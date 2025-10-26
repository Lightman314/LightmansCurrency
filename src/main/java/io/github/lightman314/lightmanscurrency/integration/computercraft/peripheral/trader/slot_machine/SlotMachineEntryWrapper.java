package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryWrapper extends AccessTrackingPeripheral {

    private final Supplier<SlotMachineEntry> entrySource;
    private final Supplier<SlotMachineTraderData> traderSource;
    public SlotMachineEntryWrapper(Supplier<SlotMachineEntry> entry, Supplier<SlotMachineTraderData> trader)
    {
        this.entrySource = entry;
        this.traderSource = trader;
    }

    @Override
    public String getType() { return "lc_slot_machine_entry"; }

    public final SlotMachineEntry getEntry() throws LuaException {
        SlotMachineEntry entry = this.entrySource.get();
        if(entry == null)
            throw new LuaException("An unexpected error occurred trying to access the entry!");
        return entry;
    }

    public int getPermissionLevel(IComputerAccess computer)
    {
        String id = this.getComputerID(computer);
        if(id == null)
            return 0;
        SlotMachineTraderData trader = this.traderSource.get();
        if(trader == null || !trader.hasAttachment(ExternalAuthorizationAttachment.TYPE))
            return 0;
        //Deny blocked permissions early
        if(trader.getBlockedPermissions().contains(Permissions.EDIT_TRADES))
            return 0;
        ExternalAuthorizationAttachment.AccessLevel access = trader.getAttachment(ExternalAuthorizationAttachment.TYPE).getAccessLevel(id);
        return switch (access) {
            case NONE -> 0;
            case ALLY -> trader.getAllyPermissionMap().getOrDefault(Permissions.EDIT_TRADES, 0);
            case ADMIN -> Integer.MAX_VALUE;
        };
    }
    public boolean hasPermission(IComputerAccess computer) { return this.getPermissionLevel(computer) > 0; }

    public final void markEntryDirty()
    {
        SlotMachineTraderData trader = this.traderSource.get();
        if(trader != null)
            trader.markEntriesDirty();
    }

    public boolean isValid() {
        try { return this.getEntry().isValid();
        } catch (LuaException exception) { return false; }
    }

    public LCLuaTable[] getItems() throws LuaException
    {
        List<LCLuaTable> list = new ArrayList<>();
        SlotMachineEntry entry = this.getEntry();
        for(ItemStack item : entry.items)
            list.add(new LCLuaTable(VanillaDetailRegistries.ITEM_STACK.getBasicDetails(item)));
        return list.toArray(LCLuaTable[]::new);
    }

    public LCLuaTable getItemDetails(IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        SlotMachineEntry entry = this.getEntry();
        ArgumentHelpers.assertBetween(slot,1,entry.items.size(),"Slot out of range (%s)");
        ItemStack item = entry.items.get(slot - 1);
        return new LCLuaTable(VanillaDetailRegistries.ITEM_STACK.getDetails(item));
    }

    public boolean addItem(IComputerAccess computer, IArguments args) throws LuaException
    {
        ItemStack item = LCArgumentHelper.parseItem(args,0);
        SlotMachineEntry entry = this.getEntry();
        if(entry.items.size() >= SlotMachineEntry.ITEM_LIMIT)
            return false;
        if(this.hasPermission(computer))
        {
            entry.TryAddItem(item);
            this.markEntryDirty();
            return true;
        }
        return false;
    }

    public boolean removeItem(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        SlotMachineEntry entry = this.getEntry();
        ArgumentHelpers.assertBetween(slot,1,entry.items.size(),"Slot out of range (%s)");
        if(this.hasPermission(computer))
        {
            entry.items.remove(slot - 1);
            this.markEntryDirty();
            return true;
        }
        return false;
    }

    public boolean editItem(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ItemStack item = LCArgumentHelper.parseItem(args,0);
        SlotMachineEntry entry = this.getEntry();
        ArgumentHelpers.assertBetween(slot,1,entry.items.size(),"Slot out of range (%s)");
        if(this.hasPermission(computer))
        {
            entry.items.set(slot - 1,item);
            this.markEntryDirty();
            return true;
        }
        return false;
    }

    public boolean isMoney() throws LuaException { return this.getEntry().isMoney(); }

    public LCLuaTable getMoneyValue() throws LuaException { return LCLuaTable.fromMoney(this.getEntry().getMoneyValue()); }

    public double getOdds() throws LuaException { return this.getEntry().getOdds(); }

    public boolean setOdds(IComputerAccess computer, IArguments args) throws LuaException
    {
        double newOdds = args.getDouble(0);
        ArgumentHelpers.assertBetween(newOdds,0.01d,99.99d,"Odds out of range (%s)");
        SlotMachineEntry entry = this.getEntry();
        if(this.hasPermission(computer))
        {
            entry.setOdds(newOdds);
            this.markEntryDirty();
            return true;
        }
        return false;
    }

    public boolean hasCustomIcons() throws LuaException { return this.getEntry().hasCustomIcons(); }

    public boolean setHasCustomIcons(IComputerAccess computer, IArguments args) throws LuaException
    {
        boolean newState = args.getBoolean(0);
        SlotMachineEntry entry = this.getEntry();
        if(this.hasPermission(computer))
        {
            entry.setHasCustomIcons(newState);
            this.markEntryDirty();
            return true;
        }
        return false;
    }

    public LCLuaTable[] getCustomIcons() throws LuaException
    {
        List<LCLuaTable> list = new ArrayList<>();
        SlotMachineEntry entry = this.getEntry();
        for(IconData icon : entry.getCustomIcons())
            list.add(LCLuaTable.fromTag(icon.save(this.registryAccess())));
        return list.toArray(LCLuaTable[]::new);
    }

    public boolean setCustomIcon(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        ArgumentHelpers.assertBetween(slot,1,SlotMachineEntry.ITEM_LIMIT,"Slot out of range (%s)");
        CompoundTag tag = LCLuaTable.toTag(args.getTable(1));
        IconData icon = IconData.load(tag,this.registryAccess());
        if(icon == null)
            throw new LuaException("Error parsing icon data");
        SlotMachineEntry entry = this.getEntry();
        if(this.hasPermission(computer))
        {
            entry.setCustomIcon(slot - 1, icon);
            this.markEntryDirty();
            return true;
        }
        return false;
    }

    @Override
    protected void registerMethods(PeripheralMethod.Registration registration) {
        registration.register(PeripheralMethod.builder("isValid").simple(this::isValid));
        registration.register(PeripheralMethod.builder("getItems").simpleArray(this::getItems));
        registration.register(PeripheralMethod.builder("getItemDetails").withArgs(this::getItemDetails));
        registration.register(PeripheralMethod.builder("addItem").withContext(this::addItem));
        registration.register(PeripheralMethod.builder("removeItem").withContext(this::removeItem));
        registration.register(PeripheralMethod.builder("editItem").withContext(this::editItem));
        registration.register(PeripheralMethod.builder("isMoney").simple(this::isMoney));
        registration.register(PeripheralMethod.builder("getMoneyValue").simple(this::getMoneyValue));
        registration.register(PeripheralMethod.builder("getOdds").simple(this::getOdds));
        registration.register(PeripheralMethod.builder("setOdds").withContext(this::setOdds));
        registration.register(PeripheralMethod.builder("hasCustomIcons").simple(this::hasCustomIcons));
        registration.register(PeripheralMethod.builder("setHasCustomIcons").withContext(this::setHasCustomIcons));
        registration.register(PeripheralMethod.builder("getCustomIcons").simpleArray(this::getCustomIcons));
        registration.register(PeripheralMethod.builder("setCustomIcon").withContext(this::setCustomIcon));
    }

}

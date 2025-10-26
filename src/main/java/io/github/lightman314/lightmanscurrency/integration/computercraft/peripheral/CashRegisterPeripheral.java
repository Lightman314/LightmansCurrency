package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CashRegisterPeripheral extends MultiTraderPeripheral {

    private final CashRegisterBlockEntity be;
    public CashRegisterPeripheral(CashRegisterBlockEntity be) { this.be = be; }

    @Override
    public String getType() { return "lc_cash_register"; }

    @Nonnull
    @Override
    protected List<TraderData> getAccessibleTraders() { return this.be.getTraders(); }

    @Override
    protected boolean stillAccessible(TraderData trader) { return this.be.getTraders().contains(trader); }

    @Override
    protected void registerMethods(PeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(PeripheralMethod.builder("getTraderPositions").simpleArray(this::getTraderPositions));
        registration.register(PeripheralMethod.builder("addTraderPosition").withArgs(this::addTraderPosition));
        registration.register(PeripheralMethod.builder("removeTraderPosition").withArgs(this::removeTraderPosition));
        registration.register(PeripheralMethod.builder("getCustomTitle").simple(this::getCustomTitle));
        registration.register(PeripheralMethod.builder("setCustomTitle").withArgs(this::setCustomTitle));
    }

    public LCLuaTable[] getTraderPositions()
    {
        List<LCLuaTable> list = new ArrayList<>();
        Level level = this.be.getLevel();
        for(BlockPos pos : this.be.traderPositions())
        {
            LCLuaTable entry = new LCLuaTable();
            entry.put("x",pos.getX());
            entry.put("y",pos.getY());
            entry.put("z",pos.getZ());
            list.add(entry);
        }
        return list.toArray(LCLuaTable[]::new);
    }

    public void addTraderPosition(IArguments args) throws LuaException
    {
        int x = args.getInt(0);
        int y = args.getInt(1);
        int z = args.getInt(2);
        List<BlockPos> pos = this.be.traderPositions();
        BlockPos newPos = new BlockPos(x,y,z);
        BlockEntity newBE = this.be.getLevel().getBlockEntity(newPos);
        if(newBE instanceof CapabilityInterfaceBlockEntity cbe)
            newBE = cbe.tryGetCoreBlockEntity();
        if(newBE instanceof TraderBlockEntity<?> tbe)
            newPos = tbe.getBlockPos();
        else
            throw new LuaException("Cannot find a trader at " + x + " " + y + " " + z);
        if(pos.contains(newPos))
            throw new LuaException("Cannot add " + x + " " + y + " " + z + " as it's already in the list of positions!");
        pos.add(newPos);
        this.be.setPositions(pos);
    }

    public void removeTraderPosition(IArguments args) throws LuaException
    {
        int x = args.getInt(0);
        int y = args.getInt(1);
        int z = args.getInt(2);
        List<BlockPos> pos = this.be.traderPositions();
        BlockPos deletePos = new BlockPos(x,y,z);
        if(pos.contains(deletePos))
        {
            pos.remove(deletePos);
            this.be.setPositions(pos);
        }
        else
            throw new LuaException("Cannot remove " + x + " " + y + " " + z + " as it's not on the list of positions!");
    }

    public String getCustomTitle()
    {
        Component title = this.be.getCustomTitle();
        return title == null ? "" : title.getString();
    }

    public void setCustomTitle(IArguments args) throws LuaException
    {
        String newTitle = args.optString(0,"");
        if(newTitle.isBlank())
            this.be.setCustomTitle(null);
        else
            this.be.setCustomTitle(EasyText.literal(newTitle));
    }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if(peripheral instanceof CashRegisterPeripheral other)
            return other.be == this.be && super.equals(peripheral);
        return false;
    }

}

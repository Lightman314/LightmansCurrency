package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CashRegisterPeripheral extends MultiTraderPeripheral {

    private final CashRegisterBlockEntity be;
    public CashRegisterPeripheral(CashRegisterBlockEntity be) { this.be = be; }

    @Override
    public String getType() { return "lc_cash_register"; }

    @Override
    protected List<TraderData> getAccessibleTraders() { return this.be.getTraders(); }

    @Override
    protected boolean stillAccessible(TraderData trader) { return this.be.getTraders().contains(trader); }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("getTraderPositions").simple(this::getTraderPositions));
        registration.register(LCPeripheralMethod.builder("addTraderPosition").withArgs(this::addTraderPosition));
        registration.register(LCPeripheralMethod.builder("removeTraderPosition").withArgs(this::removeTraderPosition));
        registration.register(LCPeripheralMethod.builder("getCustomTitle").simple(this::getCustomTitle));
        registration.register(LCPeripheralMethod.builder("setCustomTitle").withArgs(this::setCustomTitle));
    }

    public LCLuaTable getTraderPositions()
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
        return LCLuaTable.fromList(list);
    }

    public boolean addTraderPosition(IArguments args) throws LuaException
    {
        int x = args.getInt(0);
        int y = args.getInt(1);
        int z = args.getInt(2);
        List<BlockPos> pos = this.be.traderPositions();
        BlockPos newPos = new BlockPos(x,y,z);
        BlockState state = this.be.getLevel().getBlockState(newPos);
        if(state instanceof ICapabilityBlock capBlock)
            newPos = capBlock.getCapabilityBlockPos(state,this.be.getLevel(),newPos);
        BlockEntity newBE = this.be.getLevel().getBlockEntity(newPos);
        if(newBE instanceof TraderBlockEntity<?> tbe)
            newPos = tbe.getBlockPos();
        else
            return false;
        if(pos.contains(newPos))
            return false;
        pos.add(newPos);
        this.be.setPositions(pos);
        return true;
    }

    public boolean removeTraderPosition(IArguments args) throws LuaException
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
            return true;
        }
        else
            return false;
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

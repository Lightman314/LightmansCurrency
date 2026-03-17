package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate;

import dan200.computercraft.api.lua.LuaException;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.PaygateTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.trade.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TradeOfferNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate.trade.PaygateTradeWrapper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaygateTradeNodeMethods extends TradeOfferNodeMethods<PaygateTradeData,PaygateTradeNode> {

    public PaygateTradeNodeMethods(TraderPeripheral peripheral) { super(peripheral,PaygateTradeNode.TYPE); }

    @Override
    public AccessTrackingPeripheral wrapTrade(TradeData t) throws LuaException {
        if(t instanceof PaygateTradeData trade)
        {
            int index = this.getNode().indexOfTrade(trade);
            PaygateTradeWrapper wrapper = new PaygateTradeWrapper(this.tradeSource(index),this::safeGetTrader);
            wrapper.setParent(this.peripheral);
            return wrapper;
        }
        throw new LuaException("Trade is not a Paygate Trade!");
    }

    private Supplier<PaygateTradeData> tradeSource(int index)
    {
        return () -> {
            PaygateTradeNode node = this.safeGetNode();
            if(node != null && index >= 0 && index < node.getTradeCount())
                return node.getTrade(index);
            return null;
        };
    }

    @Nullable
    private PaygateBlockEntity getBlockEntity() {
        if(this.peripheral.getBlockEntity() instanceof PaygateBlockEntity be)
            return be;
        return null;
    }

    public LCLuaTable getRedstoneState() throws LuaException {
        PaygateBlockEntity be = this.getBlockEntity();
        if(be == null)
            throw new LuaException("An unexpected error occurred attempting to access the Paygate!");
        LCLuaTable table = new LCLuaTable();
        CompoundTag tag = new CompoundTag();
        be.saveRedstoneData(tag);
        Set<Direction> unusedSides = Arrays.stream(Direction.values()).collect(Collectors.toSet());
        for(var data : PaygateBlockEntity.parseVisibilityData(tag))
        {
            LCLuaTable entry = new LCLuaTable();
            if(data.name() != null)
                entry.put("Name",data.name());
            entry.put("Power",data.power());
            entry.put("Timer",data.timer());
            for(Direction side : data.sides())
            {
                table.put(side.toString(),entry);
                unusedSides.remove(side);
            }
        }
        for(Direction side : unusedSides)
            table.put(side.toString(),new LCLuaTable());
        return table;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        super.registerNodeMethods(registration);
        registration.register(LCPeripheralMethod.builder("getRedstoneState").simple(this::getRedstoneState));
    }
}

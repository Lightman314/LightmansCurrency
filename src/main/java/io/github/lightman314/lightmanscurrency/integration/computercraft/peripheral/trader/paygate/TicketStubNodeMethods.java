package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.TicketStubNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TicketStubNodeMethods extends TraderNodeMethods<TicketStubNode> {

    public TicketStubNodeMethods(TraderPeripheral peripheral) { super(peripheral,TicketStubNode.TYPE); }

    public LCLuaTable getTicketStubStorage() throws LuaException {
        List<Map<String,Object>> list = new ArrayList<>();
        for(ItemStack stub : this.getNode().getTicketStubStorage())
            list.add(VanillaDetailRegistries.ITEM_STACK.getBasicDetails(stub));
        return LCLuaTable.fromList(list);
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getTicketStubStorage").simple(this::getTicketStubStorage));
    }

}

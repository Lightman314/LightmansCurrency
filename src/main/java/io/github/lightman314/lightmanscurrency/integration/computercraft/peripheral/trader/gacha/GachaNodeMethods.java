package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.trade.GachaDummyTrade;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TradeOfferNodeMethods;

public class GachaNodeMethods extends TradeOfferNodeMethods<GachaDummyTrade,GachaNode> {

    public GachaNodeMethods(TraderPeripheral peripheral) { super(peripheral,GachaNode.TYPE); }

    @Override
    public AccessTrackingPeripheral wrapTrade(TradeData trade) { return this.peripheral; }

    public LCLuaTable getPrice() throws LuaException { return LCLuaTable.fromMoney(this.getNode().getPrice()); }

    public boolean setPrice(IComputerAccess computer, IArguments args) throws LuaException
    {
        MoneyValue newPrice = LCArgumentHelper.parseMoneyValue(args,0,true);
        GachaNode node = this.getNode();
        if(this.hasPermissions(computer, Permissions.EDIT_TRADES))
            return node.setPrice(null,newPrice);
        return false;
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(LCPeripheralMethod.builder("setPrice").withContext(this::setPrice));
    }

}

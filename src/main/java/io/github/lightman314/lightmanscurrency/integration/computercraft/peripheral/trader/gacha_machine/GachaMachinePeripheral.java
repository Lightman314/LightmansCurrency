package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha_machine;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class GachaMachinePeripheral extends TraderPeripheral<GachaMachineBlockEntity,GachaTrader> {

    public GachaMachinePeripheral(GachaMachineBlockEntity gachaMachineBlockEntity) { super(gachaMachineBlockEntity); }
    public GachaMachinePeripheral(GachaTrader trader) { super(trader); }

    @Nullable
    @Override
    protected IPeripheral wrapTrade(TradeData trade) { return this; }

    @Override
    public String getType() { return "lc_trader_gacha"; }

    public int getStorageCount() throws LuaException { return this.getTrader().getStorage().getItemCount(); }

    public int getStorageCapacity() throws LuaException { return this.getTrader().getMaxItems(); }

    public Object getStorage(IComputerAccess computer) throws LuaException { return wrapInventory(() -> this.hasPermissions(computer, Permissions.OPEN_STORAGE),this::safeGetStorage); }

    private IItemHandler safeGetStorage()
    {
        GachaTrader trader = this.safeGetTrader();
        if(trader != null)
            return trader.getStorageWrapper();
        return null;
    }

    public LCLuaTable getPrice() throws LuaException { return LCLuaTable.fromMoney(this.getTrader().getPrice()); }

    public boolean setPrice(IComputerAccess computer, IArguments args) throws LuaException
    {
        MoneyValue newPrice = LCArgumentHelper.parseMoneyValue(args,0,true);
        GachaTrader trader = this.getTrader();
        if(this.hasPermissions(computer,Permissions.EDIT_TRADES))
        {
            trader.setPrice(null,newPrice);
            return true;
        }
        return false;
    }

    @Override
    protected void registerMethods(PeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(PeripheralMethod.builder("getStorageCount").simple(this::getStorageCount));
        registration.register(PeripheralMethod.builder("getStorageCapacity").simple(this::getStorageCapacity));
        registration.register(PeripheralMethod.builder("getStorage").withContextOnly(this::getStorage));
        registration.register(PeripheralMethod.builder("getPrice").simple(this::getPrice));
        registration.register(PeripheralMethod.builder("setPrice").withContext(this::setPrice));
    }
}
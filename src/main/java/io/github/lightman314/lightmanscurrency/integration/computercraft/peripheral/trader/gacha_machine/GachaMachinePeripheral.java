package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha_machine;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GachaMachinePeripheral extends TraderPeripheral<GachaMachineBlockEntity,GachaTrader> {

    public GachaMachinePeripheral(GachaMachineBlockEntity gachaMachineBlockEntity) { super(gachaMachineBlockEntity); }
    public GachaMachinePeripheral(GachaTrader trader) { super(trader); }

    @Nullable
    @Override
    protected IPeripheral wrapTrade(TradeData trade) throws LuaException { return this; }

    @Override
    public String getType() { return "lc_trader_gacha"; }

    @LuaFunction(mainThread = true)
    public int getStorageCount() throws LuaException { return this.getTrader().getStorage().getItemCount(); }

    @LuaFunction(mainThread = true)
    public int getStorageCapacity() throws LuaException { return this.getTrader().getMaxItems(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getStorageItems() throws LuaException {
        GachaStorage storage = this.getTrader().getStorage();
        List<LCLuaTable> list = new ArrayList<>();
        for(ItemStack item : new ArrayList<>(storage.getContents()))
            list.add(LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(item,this.registryAccess())));
        return list.toArray(LCLuaTable[]::new);
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable getPrice() throws LuaException { return LCLuaTable.fromMoney(this.getTrader().getPrice()); }

}

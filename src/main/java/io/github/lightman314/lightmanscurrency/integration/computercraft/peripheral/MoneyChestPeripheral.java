package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import com.google.common.collect.ImmutableList;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MoneyChestPeripheral extends InventoryPeripheral {

    private final CoinChestBlockEntity blockEntity;
    public MoneyChestPeripheral(CoinChestBlockEntity blockEntity) {
        super(() -> true,blockEntity::getStorage,blockEntity::setChanged);
        this.blockEntity = blockEntity;
    }

    public boolean exchangeCoins(IArguments arguments) throws LuaException
    {
        //Do nothing if the exchange upgrade isn't present
        if(!this.blockEntity.getUpgrades().hasUpgrade(Upgrades.COIN_CHEST_EXCHANGE))
            return false;
        String command = arguments.getString(0);
        return ATMAPI.ExecuteATMExchangeCommand(this.blockEntity.getStorage(),command);
    }

    @Nullable
    public LCLuaTable listExchangeCommands()
    {
        //Do nothing if the exchange upgrade isn't present
        if(!this.blockEntity.getUpgrades().hasUpgrade(Upgrades.COIN_CHEST_EXCHANGE))
            return null;
        LCLuaTable table = new LCLuaTable();
        table.put("universal",LCLuaTable.fromList(ImmutableList.of("exchangeAllUp","exchangeAllDown")));
        for(ChainData chain : CoinAPI.getApi().AllChainData())
        {
            List<String> commands = new ArrayList<>();
            for(CoinEntry entry : chain.getCoreChain())
            {
                String key = BuiltInRegistries.ITEM.getKey(entry.getCoin()).toString();
                if(entry.hasLowerExchange())
                    commands.add("exchangeDown-" + key);
                if(entry.hasUpperExchange())
                    commands.add("exchangeUp-" + key);
            }
            table.put(chain.chain,LCLuaTable.fromList(commands));
        }
        return table;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("exchangeCoins").withArgs(this::exchangeCoins));
        registration.register(LCPeripheralMethod.builder("listExchangeCommands").simple(this::listExchangeCommands));
    }
}

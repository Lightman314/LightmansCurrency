package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;
import java.util.function.Consumer;

public final class MoneyAPIImpl extends MoneyAPI {

    private final Map<UUID,PlayerMoneyHolder> clientPlayerCache = new HashMap<>();
    private final Map<UUID,PlayerMoneyHolder> clientPlayerUnsafeCache = new HashMap<>();
    private final Map<UUID,PlayerMoneyHolder> serverPlayerCache = new HashMap<>();
    private final Map<UUID,PlayerMoneyHolder> serverPlayerUnsafeCache = new HashMap<>();

    public MoneyAPIImpl() {}

    @Override
    public List<CurrencyType<?>> AllCurrencyTypes() { return ImmutableList.copyOf(LCRegistries.CURRENCY_TYPE.stream().toList()); }
    
    @Override
    public IMoneyHolder GetPlayersMoneyHandler(Player player) {
        Map<UUID,PlayerMoneyHolder> cache = player.level().isClientSide ? this.clientPlayerCache : this.serverPlayerCache;
        if(!cache.containsKey(player.getUUID()))
        {
            List<IPlayerMoneyHandler> handlers = new ArrayList<>();
            for(CurrencyType<?> type : this.AllCurrencyTypes())
            {
                IPlayerMoneyHandler h = type.createMoneyHandlerForPlayer(player);
                if(h != null)
                    handlers.add(h);
            }
            cache.put(player.getUUID(), new PlayerMoneyHolder(handlers));
        }
        return cache.get(player.getUUID()).updatePlayer(player);
    }

    @Override
    public IMoneyHolder GetPlayersMoneyHandlerUnsafe(Player player) {
        Map<UUID,PlayerMoneyHolder> cache = player.level().isClientSide ? this.clientPlayerUnsafeCache : this.serverPlayerUnsafeCache;
        if(!cache.containsKey(player.getUUID()))
        {
            List<IPlayerMoneyHandler> handlers = new ArrayList<>();
            for(CurrencyType<?> type : this.AllCurrencyTypes())
            {
                IPlayerMoneyHandler h = type.createUnsafeMoneyHandlerForPlayer(player);
                if(h != null)
                    handlers.add(h);
            }
            cache.put(player.getUUID(),new PlayerMoneyHolder(handlers));
        }
        return cache.get(player.getUUID()).updatePlayer(player);
    }

    @Override
    protected IMoneyHandler CreateContainersMoneyHandler(IItemHandler container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) {
        List<IMoneyHandler> handlers = new ArrayList<>();
        for(CurrencyType<?> type : this.AllCurrencyTypes())
        {
            IMoneyHandler h = type.createMoneyHandlerForContainer(container,overflowHandler,tracker);
            if(h != null)
                handlers.add(h);
        }
        return MoneyHandler.combine(handlers);
    }

    @Override
    public IMoneyHandler GetATMMoneyHandler(Player player, IItemHandler container) {
        List<IMoneyHandler> handlers = new ArrayList<>();
        for(CurrencyType<?> type : this.AllCurrencyTypes())
        {
            IMoneyHandler h = type.createMoneyHandlerForATM(player,container);
            if(h != null)
                handlers.add(h);
        }
        return MoneyHandler.combine(handlers);
    }

    @Override
    public boolean ItemAllowedInMoneySlot(Player player, ItemStack stack) {
        for(CurrencyType<?> type : this.AllCurrencyTypes())
        {
            if(type.allowItemInMoneySlot(player,stack))
                return true;
        }
        return false;
    }
}

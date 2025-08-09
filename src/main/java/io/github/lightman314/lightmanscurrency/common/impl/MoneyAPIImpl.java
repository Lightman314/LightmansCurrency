package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MoneyAPIImpl extends MoneyAPI {

    public static final MoneyAPI INSTANCE = new MoneyAPIImpl();

    private final Map<ResourceLocation, CurrencyType> registeredCurrencyTypes = new HashMap<>();
    private final Map<UUID,PlayerMoneyHolder> clientPlayerCache = new HashMap<>();
    private final Map<UUID,PlayerMoneyHolder> serverPlayerCache = new HashMap<>();

    private MoneyAPIImpl() {}

    @Override
    public List<CurrencyType> AllCurrencyTypes() { return ImmutableList.copyOf(this.registeredCurrencyTypes.values()); }

    @Nullable
    @Override
    public CurrencyType GetRegisteredCurrencyType(ResourceLocation id) { return this.registeredCurrencyTypes.get(id); }

    @Override
    public void RegisterCurrencyType(CurrencyType type) {
        if(this.registeredCurrencyTypes.containsKey(type.getType()))
        {
            CurrencyType existingType = this.registeredCurrencyTypes.get(type.getType());
            if(existingType == type)
                LightmansCurrency.LogWarning("Money Type " + type.getType() + " was registered twice!");
            else
                LightmansCurrency.LogError("Tried to register Money Type " + type.getType() + ", but another type has already been registered under that id!");
            return;
        }
        this.registeredCurrencyTypes.put(type.getType(),type);
        LightmansCurrency.LogDebug("Registered Currency Type: " + type.getType());
    }

    @Override
    public IMoneyHolder GetPlayersMoneyHandler(Player player) {
        Map<UUID,PlayerMoneyHolder> cache = player.isLocalPlayer() ? this.clientPlayerCache : this.serverPlayerCache;
        if(!cache.containsKey(player.getUUID()))
        {
            List<IPlayerMoneyHandler> handlers = new ArrayList<>();
            for(CurrencyType type : this.registeredCurrencyTypes.values())
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
    protected IMoneyHandler CreateContainersMoneyHandler(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) {
        List<IMoneyHandler> handlers = new ArrayList<>();
        for(CurrencyType type : this.registeredCurrencyTypes.values())
        {
            IMoneyHandler h = type.createMoneyHandlerForContainer(container, overflowHandler, tracker);
            if(h != null)
                handlers.add(h);
        }
        return MoneyHandler.combine(handlers);
    }

    @Override
    public IMoneyHandler GetATMMoneyHandler(Player player, Container container) {
        List<IMoneyHandler> handlers = new ArrayList<>();
        for(CurrencyType type : this.registeredCurrencyTypes.values())
        {
            IMoneyHandler h = type.createMoneyHandlerForATM(player,container);
            if(h != null)
                handlers.add(h);
        }
        return MoneyHandler.combine(handlers);
    }

    @Override
    public boolean ItemAllowedInMoneySlot(Player player, ItemStack stack) {
        for(CurrencyType type : this.registeredCurrencyTypes.values())
        {
            if(type.allowItemInMoneySlot(player,stack))
                return true;
        }
        return false;
    }

}

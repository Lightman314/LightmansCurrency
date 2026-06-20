package io.github.lightman314.lightmanscurrency.features.api_impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.UnsortedMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

@EventBusSubscriber
public final class MoneyAPIImpl implements MoneyAPI {

    private final Map<Key,PlayerMoneyResourceWrapper> cache = new HashMap<>();

    public static final MoneyAPIImpl INSTANCE = new MoneyAPIImpl();

    private MoneyAPIImpl() {}

    @Override
    public MoneyResourceHandler getPlayersMoneyHandler(Player player, boolean allowOverflow) {
        Key key = new Key(player,allowOverflow);
        if(this.cache.containsKey(key))
        {
            PlayerMoneyResourceWrapper wrapper = this.cache.get(key);
            wrapper.updatePlayer(player);
            return wrapper;
        }
        //Assemble the wrapper
        List<PlayerMoneyResourceHandler> list = new ArrayList<>();
        for(MoneyValueHelper helper : LCRegistries.Money.VALUE_HELPER)
        {
            PlayerMoneyResourceHandler entry = helper.createMoneyHandlerForPlayer(player,allowOverflow);
            if(entry != null)
                list.add(entry);
        }
        PlayerMoneyResourceWrapper wrapper = new PlayerMoneyResourceWrapper(list);
        //Cache the result for later
        this.cache.put(key,wrapper);
        return wrapper;
    }

    @Override
    public MoneyResourceHandler getContainersMoneyHandler(ResourceHandler<ItemResource> itemResource,BiConsumer<ItemStack,TransactionContext> overflowHandler, ISidedContext context) {
        List<MoneyResourceHandler> list = new ArrayList<>();
        for(MoneyValueHelper helper : LCRegistries.Money.VALUE_HELPER)
        {
            MoneyResourceHandler handler = helper.wrapContainer(itemResource,overflowHandler,context);
            if(handler != null)
                list.add(handler);
        }
        return new UnsortedMoneyResourceHandler(list);
    }

    @SubscribeEvent
    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) { INSTANCE.onPlayerLeave(event.getEntity()); }

    public void onPlayerLeave(@Nullable Player player)
    {
        //Null-check for safety, as otherwise the game likes to crash if something else went wrong
        if(player == null)
            return;
        Key k1 = new Key(player,true);
        Key k2 = new Key(player,false);
        this.cache.remove(k1);
        this.cache.remove(k2);
    }

    record Key(UUID playerID,boolean client,boolean allowOverflow) {
        Key(Player player,boolean allowOverflow) { this(player.getUUID(),player.level().isClientSide(),allowOverflow); }
    }

    private static class PlayerMoneyResourceWrapper extends UnsortedMoneyResourceHandler {

        private final List<PlayerMoneyResourceHandler> children;

        public PlayerMoneyResourceWrapper(List<PlayerMoneyResourceHandler> children) {
            super(children);
            this.children = ImmutableList.copyOf(children);
        }

        public void updatePlayer(Player player)
        {
            for(PlayerMoneyResourceHandler child : this.children)
                child.updatePlayer(player);
        }

    }

}

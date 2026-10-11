package io.github.lightman314.lightmanscurrency.features.api_impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.OverflowHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.ItemCapabilityResourceWrapper;
import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.UnsortedMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.UnaryOperator;

@EventBusSubscriber
public final class MoneyAPIImpl implements MoneyAPI {

    private final Map<UUID,PlayerMoneyResourceWrapper> cache = new HashMap<>();

    public static final MoneyAPIImpl INSTANCE = new MoneyAPIImpl();

    private MoneyAPIImpl() {}

    @Override
    public SortableMoneyResourceHandler getPlayersMoneyHandler(Player player) {
        UUID key = player.getUUID();
        if(this.cache.containsKey(key))
        {
            PlayerMoneyResourceWrapper wrapper = this.cache.get(key);
            wrapper.updatePlayer(player);
            return wrapper;
        }
        PlayerMoneyResourceWrapper wrapper = this.createPlayersMoneyHandler(player,UnaryOperator.identity());
        this.cache.put(key,wrapper);
        return wrapper;
    }

    @Override
    public SortableMoneyResourceHandler getPlayersMoneyHandler(Player player,@Nullable UnaryOperator<OverflowHandler> overflowModifier) {
        if(overflowModifier == null)
            return this.getPlayersMoneyHandler(player);
        return this.createPlayersMoneyHandler(player,overflowModifier);
    }

    private PlayerMoneyResourceWrapper createPlayersMoneyHandler(Player player,UnaryOperator<OverflowHandler> overflowModifier) {

        //Assemble the wrapper
        List<PlayerMoneyResourceHandler> list = new ArrayList<>();
        for(MoneyValueHelper helper : LCRegistries.Money.VALUE_HELPER)
        {
            PlayerMoneyResourceHandler entry = helper.createMoneyHandlerForPlayer(player,overflowModifier);
            if(entry != null)
                list.add(entry);
        }
        return new PlayerMoneyResourceWrapper(list);
    }

    @Override
    public MoneyResourceHandler getContainersMoneyHandler(ResourceHandler<ItemResource> itemResource,OverflowHandler overflowHandler, ISidedContext context, boolean wrapCapabilities) {
        List<MoneyResourceHandler> list = new ArrayList<>();
        if(wrapCapabilities)
            list.add(new ItemCapabilityResourceWrapper(itemResource,context));
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
        this.cache.remove(player.getUUID());
    }

    record Key(UUID playerID,boolean client,boolean allowOverflow) {
        Key(Player player,boolean allowOverflow) { this(player.getUUID(),player.level().isClientSide(),allowOverflow); }
    }

    private static class PlayerMoneyResourceWrapper extends UnsortedMoneyResourceHandler implements SortableMoneyResourceHandler {

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

        @Override
        public Component getMoneyCategoryTitle() { return SortableMoneyResourceHandler.TOOLTIP_MONEY_SOURCE_PLAYER.get(); }

    }

}

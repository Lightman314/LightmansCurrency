package io.github.lightman314.lightmanscurrency.api.trader.event;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;
import java.util.function.Function;

public abstract class TraderEvent extends Event implements ISidedContext {

    @Override
    public final boolean isClient() { return this.getTrader().isClient(); }
    public abstract long getTraderID();
    public abstract TraderData getTrader();
    public TraderType getTraderType() { return this.getTrader().getType(); }

    /**
     * Default abstract implementation of {@link TraderEvent}, that has the TraderData passed to it directly
     */
    public static abstract class Direct extends TraderEvent
    {
        private final TraderData trader;
        @Override
        public final long getTraderID() { return this.trader.getID(); }
        @Override
        public final TraderData getTrader() { return this.trader; }
        public Direct(TraderData trader) { this.trader = trader; }
    }

    public static class RegisterNodesEvent extends Direct implements NodeCollector
    {
        private final NodeCollector collector;
        @ApiStatus.Internal
        public RegisterNodesEvent(TraderData trader,NodeCollector collector) { super(trader); this.collector = collector; }
        @Override
        public void addNode(TraderNodeType<?> node) { this.collector.addNode(node); }
        @Override
        public void addArgument(Identifier key, Object argument) { this.collector.addArgument(key,argument); }
        @Override
        public void removeNode(TraderNodeType<?> node) { this.collector.removeNode(node); }
        @Override
        public Iterator<TraderNodeType<?>> iterator() { return this.collector.iterator(); }
    }

    /**
     * @see Pre
     * @see Post
     */
    public static sealed class StorageMenuTabsEvent extends Direct implements StorageTabBuilder {

        private final StorageTabBuilder builder;
        public StorageMenuTabsEvent(TraderData trader,StorageTabBuilder builder) { super(trader); this.builder = builder; }

        public final Player getPlayer() { return this.menu().getPlayer(); }
        @Override
        public final TraderStorageMenu menu() { return this.builder.menu(); }
        @Override
        public final void addTab(Function<TraderStorageMenu,TraderStorageTab> factory) { StorageTabBuilder.super.addTab(factory); }
        @Override
        public final void addTab(TraderStorageTab tab) { this.builder.addTab(tab); }
        @Override
        public void removeTab(Identifier tabKey) { this.builder.removeTab(tabKey); }
        @Override
        public boolean hasTab(Identifier tabKey) { return this.builder.hasTab(tabKey); }

        /**
         * Posted when the Trader Storage Menu is opened by a player, but before the built-in tabs are collected from the traders nodes
         */
        public static final class Pre extends StorageMenuTabsEvent {
            @ApiStatus.Internal
            public Pre(TraderData trader,StorageTabBuilder builder) { super(trader, builder); }
        }

        /**
         * Posted when the Trader Storage Menu is opened by a player, after the built-in tabs are collected from the traders nodes
         */
        public static final class Post extends StorageMenuTabsEvent {
            @ApiStatus.Internal
            public Post(TraderData trader,StorageTabBuilder builder) { super(trader, builder); }
        }

    }

    /**
     * Posted when a Trader is first created and registered via {@link TraderAPI#initializeTrader(TraderData)}
     */
    public static class TraderCreatedEvent extends Direct {
        @ApiStatus.Internal
        public TraderCreatedEvent(TraderData trader) { super(trader); }
    }

    /**
     * Posted when a Trader is deleted via {@link TraderAPI#deleteTrader(long)}
     */
    public static class TraderDeletedEvent extends Direct {
        @ApiStatus.Internal
        public TraderDeletedEvent(TraderData trader) { super(trader); }
    }

}
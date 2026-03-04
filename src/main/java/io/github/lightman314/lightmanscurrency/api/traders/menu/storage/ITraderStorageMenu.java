package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.IEasyTabbedMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ITraderStorageMenu extends IEasyTabbedMenu<TraderStorageTab> {

    void addTab(TraderStorageTab tab);
    void clearTab(ResourceLocation tabKey);

    void ChangeTab(ResourceLocation tabKey);
    void ChangeTab(ResourceLocation tabKey,@Nullable LazyPacketData.Builder data);
    void ChangeTab(ResourceLocation tabKey,@Nullable LazyPacketData data);

    TradeContext getContext();

    @Nullable
    TraderData getTrader();
    @Nullable
    default <T extends TraderNode> T getTraderNode(TraderNodeType<T> type){
        TraderData trader = this.getTrader();
        if(trader == null)
            return null;
        return trader.getNode(type);
    }
    default <T extends TraderNode> void ifNodePresent(TraderNodeType<T> type, Consumer<T> action) {
        TraderData trader = this.getTrader();
        if(trader != null)
            trader.ifNodePresent(type,action);
    }
    @Nullable
    default <R,T extends TraderNode> R findNodeValue(TraderNodeType<T> type, Function<T,R> getter) { return this.findNodeValueOrDefault(type,getter,null); }
    @Nullable
    default <R,T extends TraderNode> R findNodeValueOrDefault(TraderNodeType<T> type, Function<T,R> getter, @Nullable R defaultValue) {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.findNodeValue(type,getter,defaultValue);
        return defaultValue;
    }

    @Nullable
    default TradeOfferSourceNode<?> getTradeOfferNode()
    {
        TraderData trader = this.getTrader();
        if(trader == null)
            return null;
        return trader.getTradeOfferNode();
    }
    @Nullable
    default <T> T findTradeOfferNodeValue(Function<TradeOfferSourceNode<?>,T> getter) { return this.findTradeOfferNodeValue(getter,null); }
    @Nullable
    default <T> T findTradeOfferNodeValue(Function<TradeOfferSourceNode<?>,T> getter,@Nullable T defaultValue) {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.findTradeOfferNodeValue(getter,defaultValue);
        return defaultValue;
    }
    
    Player getPlayer();
    
    ItemStack getHeldItem();
    void setHeldItem(ItemStack stack);
    void clearContainer(Container container);
    void clearContainer(IItemHandler container);

    void SendMessage(LazyPacketData.Builder message);

    default boolean hasPermission(String permission) { return this.getPermissionLevel(permission) > 0; }
    int getPermissionLevel(String permission);

}

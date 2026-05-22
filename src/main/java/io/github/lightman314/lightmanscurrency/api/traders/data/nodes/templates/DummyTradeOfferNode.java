package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.trade.DummyTrade;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Implementation of {@link TradeOfferSourceNode} but for traders that don't store trade-related data within a trade themselves.<br>
 * Primarily used for Traders like the Slot Machine or Gacha Machine that are hard-coded to only have 1 trade.
 * @param <T> The Trade Data class used for the dummy trade
 */
public abstract class DummyTradeOfferNode<T extends DummyTrade> extends TradeOfferSourceNode<T> {

    @Override
    public void onAttach() {
        this.getTrade().updateTrader(this.trader);
    }

    protected abstract T getTrade();

    @Override
    public final boolean addTrade(Player player) { return false; }
    @Override
    public final boolean removeTrade(Player player) { return false; }
    @Override
    public final boolean supportsTradeRules() { return false; }
    @Override
    protected final List<T> getEditableList() { return Lists.newArrayList(this.getTrade()); }
    @Override
    public final boolean canChangeQuantity() { return false; }
    @Override
    public final boolean canEasilyChangeQuantity() { return false; }
    @Override
    public final boolean canUpgradeChangeQuantity() { return super.canUpgradeChangeQuantity(); }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        //Don't call the super method, but otherwise don't do anything weird
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        //Don't call the super method, but otherwise don't do anything weird
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        //Don't call the super method, but otherwise don't do anything weird
    }

    @Override
    public final void setTradeChanged(int tradeIndex) { }

}

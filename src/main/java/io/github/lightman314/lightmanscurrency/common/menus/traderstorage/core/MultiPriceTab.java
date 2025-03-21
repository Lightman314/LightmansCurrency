package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core.MultiPriceClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiPriceTab extends TraderStorageTab {

    public MultiPriceTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

    private final List<Integer> selectedTrades = new ArrayList<>();
    public int selectedCount() { return this.selectedTrades.size(); }

    public List<TradeData> getSelectedTrades()
    {
        List<TradeData> list = new ArrayList<>();
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return list;
        for(int index : this.selectedTrades)
        {
            TradeData trade = trader.getTrade(index);
            if(trade != null)
                list.add(trade);
        }
        return list;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new MultiPriceClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    public void setPrices(@Nonnull MoneyValue price)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            //Edit All Trades
            for(TradeData trade : this.getSelectedTrades())
                trade.setCost(price);
            trader.markTradesDirty();
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("SetAllPrices",price));
            else
                this.menu.ChangeTab(TraderStorageTab.TAB_TRADE_BASIC);
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SetAllPrices"))
            this.setPrices(message.getMoneyValue("SetAllPrices"));
    }

    @Override
    public void OpenMessage(@Nullable LazyPacketData message) {
        this.selectedTrades.clear();
        this.selectedTrades.addAll(message.getList("Selected",LazyPacketData::getInt));
        LightmansCurrency.LogDebug("Parsing Selected Trades on the " + DebugUtil.getSideText(this) + "\nSelected: " + DebugUtil.debugList(this.selectedTrades));
    }

    @Override
    public void onTabClose() { this.selectedTrades.clear(); }

}

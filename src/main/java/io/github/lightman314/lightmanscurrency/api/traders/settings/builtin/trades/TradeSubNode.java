package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public abstract class TradeSubNode<T extends TradeData,N extends SettingsNode> extends SettingsSubNode<N> {

    protected final int index;
    public TradeSubNode(N parent,int index) {
        super(parent);
        this.index = index;
    }

    @Nullable
    protected abstract T getTrade();

    @Override
    public String getSubKey() { return "trade_" + this.index; }

    @Override
    public MutableComponent getName() {
        return LCText.DATA_ENTRY_TRADER_TRADES.get(this.index + 1);
    }

    @Override
    public boolean allowLoading(LoadContext context) { return context.hasPermission(Permissions.EDIT_TRADES); }

    @Override
    public final void saveSettings(SavedSettingData.MutableNodeAccess data) {
        T trade = this.getTrade();
        if(trade != null)
            this.saveTrade(data,trade);
    }

    protected abstract void saveTrade(SavedSettingData.MutableNodeAccess node, T trade);

    @Override
    public final void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        T trade = this.getTrade();
        if(trade != null)
            this.loadTrade(data,trade,context);
    }

    protected abstract void loadTrade(SavedSettingData.NodeAccess node, T trade, LoadContext context);

}
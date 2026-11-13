package io.github.lightman314.lightmanscurrency.api.trader.nodes.templates;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IFlexibleTradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class FlexibleSizedTradingNode<T extends TradeData> extends TradingNode<T> implements IFlexibleTradingNode, IDisplayNode {

    private final List<T> trades = new ArrayList<>();

    protected FlexibleSizedTradingNode() {}
    protected FlexibleSizedTradingNode(List<T> trades) {
        this.trades.addAll(trades);
        this.attachTrades();
    }

    @Override
    protected List<T> getMutableTrades() { return this.trades; }

    @Override
    public boolean showTradeCountButtons(Player player) { return this.getPermission(player,LCPermissions.EDIT_TRADES); }

    @Override
    public void onAttach() {
        if(this.trades.isEmpty())
            this.forceTradeCount(1);
    }

    @Override
    public boolean canAddTrade(Player player) {
        if(LCApi.isInAdminMode(player))
            return this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT;
        return this.getPermission(player,LCPermissions.EDIT_TRADES) && this.getTradeCount() < this.getMaxTradeCount();
    }

    @Override
    public boolean canRemoveTrade(Player player) {
        return this.getPermission(player,LCPermissions.EDIT_TRADES) && this.trades.size() > 1;
    }

    @Override
    public boolean tryAddTrade(Player player) {
        if(this.isClient())
            return false;
        if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
            return false;
        if(this.canAddTrade(player)) {
            this.forceTradeCount(this.getTradeCount() + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryRemoveTrade(Player player) {
        if(this.isClient())
            return false;
        if(this.getTradeCount() <= 1)
            return false;
        if(this.canRemoveTrade(player)) {
            this.forceTradeCount(this.getTradeCount() - 1);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Integer> getNameColor() { return this.getTerminalTradeColor(); }

    public abstract int getMaxTradeCount();

    @Override
    public void appendTerminalText(@Nullable Player player, Consumer<Component> builder) {
        this.appendTerminalTradeStatus(builder);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void encodeSettings(ValueOutput output) {
        output.putInt("trade_count",this.trades.size());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void decodeSettings(ValueInput data,SettingsLoadContext context) {
        int newCount = data.getIntOr("trade_count",1);
        if(newCount > this.getMaxTradeCount() && !context.isAdminPlayer())
            newCount = this.getMaxTradeCount();
        this.forceTradeCount(Math.max(1,newCount));
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void appendDisplay(ValueInput data, SettingsDisplayOutput output) {
        output.acceptTitle(this.getSettingsName());
        output.acceptEntry(VALUE_TRADE_COUNT,data.getIntOr("trade_count",0));
    }

}

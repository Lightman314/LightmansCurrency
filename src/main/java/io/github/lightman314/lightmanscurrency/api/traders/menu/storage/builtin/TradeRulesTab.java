package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public abstract class TradeRulesTab extends TraderStorageTab {

    public static final ResourceLocation TRADER_KEY = LightmansCurrency.id("trade_rules/trader");
    public static final ResourceLocation TRADE_KEY = LightmansCurrency.id("trade_rules/trade");

    @Nullable
    public abstract ITradeRuleHost getHost();

    protected TradeRulesTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADE_RULES); }

    public void EditTradeRule(TradeRuleType<?> type, LazyPacketData.Builder updateMessage) { EditTradeRule(LCRegistries.TRADE_RULE.getKey(type), updateMessage); }
    public void EditTradeRule(ResourceLocation type, LazyPacketData.Builder updateMessage)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADE_RULES))
            return;
        ITradeRuleHost host = this.getHost();
        if(host != null)
            host.HandleRuleUpdate(this.menu.getPlayer(), type, updateMessage.build());
        if(this.menu.isClient())
            this.menu.SendMessage(updateMessage.setString("TradeRuleEdit", type.toString()));
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("TradeRuleEdit"))
        {
            ResourceLocation type = ResourceLocation.parse(message.getString("TradeRuleEdit"));
            this.EditTradeRule(type, message.copyToBuilder());
        }
    }

    public boolean hasBackButton() { return false; }

    public void goBack() {}

    public static class Trader extends TradeRulesTab {

        public Trader(ITraderStorageMenu menu) { super(menu); }

        @Override
        public ResourceLocation tabKey() { return TRADER_KEY; }

        @Override
        public Object createClientTab(Object screen) { return new TradeRulesClientTab.Trader(screen, this); }

        @Nullable
        @Override
        public ITradeRuleHost getHost() { return this.menu.getTrader(); }

    }

    public static class Trade extends TradeRulesTab {

        private ResourceLocation previousTab = null;
        private int tradeIndex = -1;
        public int getTradeIndex() { return this.tradeIndex; }
        public Trade(ITraderStorageMenu menu) { super(menu); }

        @Override
        public ResourceLocation tabKey() { return TRADE_KEY; }

        @Override
        public Object createClientTab(Object screen) { return new TradeRulesClientTab.Trade(screen, this); }

        @Override
        public boolean canOpen(Player player) { return super.canOpen(player) && this.menu.hasPermission(Permissions.EDIT_TRADES); }

        @Override
        public boolean hasBackButton() { return this.previousTab != null; }

        @Override
        public void goBack() {
            if(this.previousTab == null)
                return;
            this.menu.ChangeTab(this.previousTab,this.builder().setInt("TradeIndex", this.tradeIndex));
        }

        @Nullable
        @Override
        public ITradeRuleHost getHost() {
            TraderData trader = this.menu.getTrader() ;
            if(trader != null)
            {
                TradeOfferSourceNode<?> node = trader.getTradeOfferNode();
                if(node != null && node.getTrade(this.tradeIndex) instanceof ITradeRuleHost host)
                    return host;
            }
            return null;
        }

        @Override
        public void OpenMessage(LazyPacketData message) {
            if(message.contains("TradeIndex"))
                this.tradeIndex = message.getInt("TradeIndex");
            if(message.contains("BackSlot"))
                this.previousTab = message.getResourceLocation("BackSlot");
            else
                this.previousTab = null;
        }

    }

}

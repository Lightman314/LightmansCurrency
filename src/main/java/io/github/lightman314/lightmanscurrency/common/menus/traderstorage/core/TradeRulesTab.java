package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TradeRulesTab extends TraderStorageTab {

    @Nullable
    public abstract ITradeRuleHost getHost();

    protected TradeRulesTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADE_RULES); }

    public void EditTradeRule(@Nonnull TradeRuleType<?> type, @Nonnull LazyPacketData.Builder updateMessage) { EditTradeRule(type.type, updateMessage); }
    public void EditTradeRule(@Nonnull ResourceLocation type, @Nonnull LazyPacketData.Builder updateMessage)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADE_RULES))
            return;
        ITradeRuleHost host = this.getHost();
        if(host != null)
            host.HandleRuleUpdate(type, updateMessage.build());
        if(this.menu.isClient())
            this.menu.SendMessage(updateMessage.setString("TradeRuleEdit", type.toString()));
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("TradeRuleEdit"))
        {
            ResourceLocation type = VersionUtil.parseResource(message.getString("TradeRuleEdit"));
            this.EditTradeRule(type, message.copyToBuilder());
        }
    }

    public boolean hasBackButton() { return false; }

    public void goBack() {}

    public static class Trader extends TradeRulesTab {

        public Trader(TraderStorageMenu menu) { super(menu); }

        @Nonnull
        @Override
        @OnlyIn(Dist.CLIENT)
        public Object createClientTab(@Nonnull Object screen) { return new TradeRulesClientTab.Trader(screen, this); }

        @Nullable
        @Override
        public ITradeRuleHost getHost() { return this.menu.getTrader(); }

    }

    public static class Trade extends TradeRulesTab {

        private int tradeIndex = -1;
        public int getTradeIndex() { return this.tradeIndex; }
        public Trade(TraderStorageMenu menu) { super(menu); }

        @Nonnull
        @Override
        @OnlyIn(Dist.CLIENT)
        public Object createClientTab(@Nonnull Object screen) { return new TradeRulesClientTab.Trade(screen, this); }

        @Override
        public boolean canOpen(Player player) { return super.canOpen(player) && this.menu.hasPermission(Permissions.EDIT_TRADES); }

        @Override
        public boolean hasBackButton() { return true; }

        @Override
        public void goBack() {
            this.menu.ChangeTab(TraderStorageTab.TAB_TRADE_ADVANCED, this.builder().setInt("TradeIndex", this.tradeIndex));
        }

        @Nullable
        @Override
        public ITradeRuleHost getHost() {
            TraderData trader = this.menu.getTrader() ;
            if(trader != null)
                return trader.getTrade(this.tradeIndex);
            return null;
        }

        @Override
        public void OpenMessage(LazyPacketData message) {
            if(message.contains("TradeIndex"))
                this.tradeIndex = message.getInt("TradeIndex");
        }


    }

}
package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public abstract class TradeRulesTab extends TraderStorageTab {

    @Nullable
    public abstract ITradeRuleHost getHost();

    protected TradeRulesTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADE_RULES); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void EditTradeRule(@Nonnull ResourceLocation type, @Nonnull CompoundTag updateMessage)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADE_RULES))
            return;
        ITradeRuleHost host = this.getHost();
        if(host != null)
            host.HandleRuleUpdate(type, updateMessage);
        if(this.menu.isClient())
        {
            this.menu.SendMessage(LazyPacketData.builder()
                    .setString("TradeRuleEdit", type.toString())
                    .setCompound("UpdateMessage", updateMessage));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("TradeRuleEdit"))
        {
            ResourceLocation type = new ResourceLocation(message.getString("TradeRuleEdit"));
            CompoundTag updateMessage = message.getNBT("UpdateMessage");
            this.EditTradeRule(type, updateMessage);
        }
    }

    public static class Trader extends TradeRulesTab {

        public Trader(TraderStorageMenu menu) { super(menu); }


        @Override
        @OnlyIn(Dist.CLIENT)
        public Object createClientTab(Object screen) { return new TradeRulesClientTab.Trader(screen, this); }

        @Nullable
        @Override
        public ITradeRuleHost getHost() { return this.menu.getTrader(); }

    }

    public static class Trade extends TradeRulesTab {

        private int tradeIndex = -1;
        public int getTradeIndex() { return this.tradeIndex; }
        public Trade(TraderStorageMenu menu) { super(menu); }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Object createClientTab(Object screen) { return new TradeRulesClientTab.Trade(screen, this); }

        @Override
        public boolean canOpen(Player player) { return super.canOpen(player) && this.menu.hasPermission(Permissions.EDIT_TRADES); }

        @Nullable
        @Override
        public ITradeRuleHost getHost() {
            TraderData trader = this.menu.getTrader() ;
            if(trader != null)
                return trader.getTrade(this.tradeIndex);
            return null;
        }

        @Override
        public void receiveMessage(LazyPacketData message) {
            super.receiveMessage(message);
            if(message.contains("TradeIndex"))
                this.tradeIndex = message.getInt("TradeIndex");
        }
    }

}

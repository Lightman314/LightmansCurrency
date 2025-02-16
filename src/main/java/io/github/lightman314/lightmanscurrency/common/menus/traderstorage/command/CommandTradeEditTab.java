package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.command;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.command.CommandTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class CommandTradeEditTab extends TraderStorageTab {

    public CommandTradeEditTab(ITraderStorageMenu menu) { super(menu); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new CommandTradeEditClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    @Override
    public void onTabOpen() { this.menu.SetCoinSlotsActive(false); }

    @Override
    public void onTabClose() { this.menu.SetCoinSlotsActive(true); }

    private int tradeIndex = -1;
    public int getTradeIndex() { return this.tradeIndex; }
    public CommandTrade getTrade() {
        if(this.menu.getTrader() instanceof CommandTrader trader)
        {
            if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
            {
                this.menu.ChangeTab(TraderStorageTab.TAB_TRADE_BASIC);
                return null;
            }
            return trader.getTrade(this.tradeIndex);
        }
        return null;
    }

    public void setCommand(String newCommand)
    {
        CommandTrade trade = this.getTrade();
        if(trade != null)
        {
            trade.setCommand(newCommand);
            this.menu.getTrader().markTradesDirty();
            if(this.isClient())
                this.menu.SendMessage(this.builder().setString("NewCommand",newCommand));
        }
    }

    public void setPrice(MoneyValue price)
    {
        CommandTrade trade = this.getTrade();
        if(trade != null)
        {
            trade.setCost(price);
            this.menu.getTrader().markTradesDirty();
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("NewPrice",price));
        }
    }

    @Override
    public void OpenMessage(@Nonnull LazyPacketData message) {
        if(message.contains("TradeIndex"))
            this.tradeIndex = message.getInt("TradeIndex");
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("NewCommand"))
            this.setCommand(message.getString("NewCommand"));
        else if(message.contains("NewPrice"))
            this.setPrice(message.getMoneyValue("NewPrice"));
    }

}

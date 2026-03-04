package io.github.lightman314.lightmanscurrency.common.traders.commands.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.client.tabs.CommandTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.nodes.CommandTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.commands.trade.CommandTrade;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CommandTradeEditTab extends TraderStorageNodeTab<CommandTradeNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("command_trade_edit");

    public CommandTradeEditTab(ITraderStorageMenu menu) { super(CommandTradeNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public Object createClientTab(Object screen) { return new CommandTradeEditClientTab(screen,this); }

    @Override
    protected boolean canOpenTab(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    private int tradeIndex = -1;
    public int getTradeIndex() { return this.tradeIndex; }
    public CommandTrade getTrade() {
        CommandTradeNode node = this.getNode();
        if(node != null)
        {
            if(this.tradeIndex >= node.getTradeCount() || this.tradeIndex < 0)
            {
                this.menu.ChangeTab(0);
                return null;
            }
            return node.getTrade(this.tradeIndex);
        }
        return null;
    }

    public void setCommand(String newCommand)
    {
        CommandTrade trade = this.getTrade();
        if(trade != null)
        {
            trade.setCommand(newCommand);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setString("NewCommand",newCommand));
        }
    }

    public void setDescription(String newDescription)
    {
        CommandTrade trade = this.getTrade();
        if(trade != null)
        {
            trade.setDescription(newDescription);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setString("NewDescription",newDescription));
        }
    }

    public void setTooltip(String newTooltip)
    {
        CommandTrade trade = this.getTrade();
        if(trade != null)
        {
            trade.setTooltip(newTooltip);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setString("NewTooltip",newTooltip));
        }
    }

    public void setPrice(MoneyValue price)
    {
        CommandTrade trade = this.getTrade();
        if(trade != null)
        {
            trade.setCost(price);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("NewPrice",price));
        }
    }

    @Override
    public void OpenMessage(LazyPacketData message) {
        if(message.contains("TradeIndex"))
            this.tradeIndex = message.getInt("TradeIndex");
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("NewCommand"))
            this.setCommand(message.getString("NewCommand"));
        if(message.contains("NewDescription"))
            this.setDescription(message.getString("NewDescription"));
        if(message.contains("NewTooltip"))
            this.setTooltip(message.getString("NewTooltip"));
        if(message.contains("NewPrice"))
            this.setPrice(message.getMoneyValue("NewPrice"));
    }

}

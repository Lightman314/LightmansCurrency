package io.github.lightman314.lightmanscurrency.common.traders.commands.settings;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.commands.nodes.CommandTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.commands.trade.CommandTrade;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CommandTradeSettings extends TradeSettings<CommandTrade, CommandTradeNode> {

    public CommandTradeSettings(TraderData trader, CommandTradeNode node) {
        super("command_trades", trader, node);
    }

    @Override
    protected SettingsSubNode<?> createTradeNode(int tradeIndex) { return new TradeNode(this,tradeIndex); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setIntValue("permissionLevel",this.node.getPermissionLevel());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        this.node.setPermissionLevel(null,data.getIntValue("permissionLevel"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        //TODO display info related to the permission level
    }

    private static class TradeNode extends TradeSubNode<CommandTrade,CommandTradeSettings>
    {

        public TradeNode(CommandTradeSettings parent, int index) { super(parent, index); }

        @Nullable
        @Override
        protected CommandTrade getTrade() { return this.parent.node.getTrade(this.index); }

        @Override
        protected void saveTrade(SavedSettingData.MutableNodeAccess node, CommandTrade trade) {
            node.setCustom("price",trade.getCost(),MoneyValue.CODEC);
            node.setStringValue("command",trade.getCommand());
            node.setStringValue("description",trade.getDescription());
            node.setStringValue("tooltip",trade.getTooltip());
        }

        @Override
        protected void loadTrade(SavedSettingData.NodeAccess node, CommandTrade trade, LoadContext context) {
            trade.setCost(node.getCustomValue("price",MoneyValue.CODEC,MoneyValue.empty()));
            trade.setCommand(node.getStringValue("command"));
            trade.setDescription(node.getStringValue("description"));
            trade.setTooltip(node.getStringValue("tooltip"));
        }

        @Override
        protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {

        }

    }

}

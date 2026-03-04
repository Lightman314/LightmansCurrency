package io.github.lightman314.lightmanscurrency.common.traders.commands;

import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.templates.PersistentSupportingTraderData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.CommandTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.commands.nodes.CommandTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.commands.trade.CommandTrade;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Map;

public class CommandTrader extends PersistentSupportingTraderData {

    public static final TraderType<CommandTrader> TYPE = TraderType.simple(CommandTrader::new,CommandTrader::new);

    private CommandTrader() {super(); }
    public CommandTrader(Level level, BlockPos pos) { super(false,level,pos); }
    private CommandTrader(long id,Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    @Override
    public TraderType<?> getType() { return TYPE; }

    @Override
    public void addCustomNodes(NodeCollector collector) {
        super.addDefaultNodes(collector);
        collector.addNode(CommandTradeNode.TYPE);
        UpgradesNode.addNode(collector,1,true);
    }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.COMMAND_BLOCK); }

    @Override
    protected TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
        CommandTradeNode tradeNode = this.assertNode(CommandTradeNode.TYPE);
        MoneyStorageNode moneyNode = this.assertNode(MoneyStorageNode.TYPE);
        CommandTrade trade = tradeNode.getTrade(tradeIndex);
        if(trade == null || !trade.isValid() || moneyNode == null)
            return TradeResult.FAIL_INVALID_TRADE;
        if(context.getPlayer() instanceof ServerPlayer player)
        {

            if(this.runPreTradeEvent(trade,context).isCanceled())
                return TradeResult.FAIL_TRADE_RULE_DENIAL;

            MoneyValue price = trade.getCost(context);
            if(!context.getPayment(price))
                return TradeResult.FAIL_CANNOT_AFFORD;

            //Give the paid cost to storage
            MoneyValue taxesPaid = MoneyValue.empty();
            if(this.shouldStoreMoney())
                taxesPaid = moneyNode.addStoredMoney(price, context.getTaxContext());

            //Run Command
            player.server.getCommands().performPrefixedCommand(this.sourceForPlayer(player,tradeNode),trade.formatCommand(player));

            //Handle Stats
            this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
            if(!taxesPaid.isEmpty())
                this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

            //Push Notification
            this.pushNotification(CommandTradeNotification.create(trade,price,context.getPlayerReference(),this.getNotificationCategory(),taxesPaid));

            //Push the post-trade event
            return this.runPostTradeEvent(trade,context,price,taxesPaid,trade.getCommand());

        }
        else
            return TradeResult.FAIL_NOT_SUPPORTED;
    }

    private CommandSourceStack sourceForPlayer(ServerPlayer player,CommandTradeNode node) {
        return new CommandSourceStack(player,player.position(),player.getRotationVector(),player.serverLevel(),node.getPermissionLevel(),player.getName().getString(),player.getName(),player.server,player);
    }

}

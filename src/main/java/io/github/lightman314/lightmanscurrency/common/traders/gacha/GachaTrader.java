package io.github.lightman314.lightmanscurrency.common.traders.gacha;

import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.templates.NetworkSupportingTraderData;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.GachaItemHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.GachaBallItem;
import io.github.lightman314.lightmanscurrency.common.menus.gacha_machine.GachaMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.GachaTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.TraderColorNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.trade.GachaDummyTrade;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GachaTrader extends NetworkSupportingTraderData {

    public static final TraderType<GachaTrader> TYPE = TraderType.simple(GachaTrader::new,GachaTrader::new);

    protected GachaTrader() { }
    public GachaTrader(Level level, BlockPos pos, int color) { super(buildArgs(color),false,level,pos); }
    private GachaTrader(long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    protected static Map<TraderNodeType<?>,Object> buildArgs(int color) { return buildArgs(new HashMap<>(),color); }
    protected static Map<TraderNodeType<?>,Object> buildArgs(Map<TraderNodeType<?>,Object> map, int color) {
        map.put(TraderColorNode.TYPE,color);
        return map;
    }

    @Override
    public TraderType<?> getType() { return TYPE; }

    @Override
    protected void addCustomNodes(NodeCollector collector) {
        collector.addNode(UpgradesNode.TYPE,5);
        collector.addNode(TraderColorNode.TYPE);
        collector.addNode(GachaStorageNode.TYPE);
        collector.addNode(GachaNode.TYPE);
    }

    private final GachaItemHandler handler = new GachaItemHandler(this);
    public IItemHandler getStorageWrapper() { return this.handler.getFullyAuthorizedHandler(); }

    public IItemHandler getItemHandler(Direction side) { return this.handler.getHandler(side); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(GachaBallItem.createWithItemAndColor(new ItemStack(ModItems.TRADING_CORE.get()),Color.YELLOW)); }

    @Override
    protected MenuProvider getTraderMenuProvider(MenuValidator validator) { return new GachaMachineMenuProvider(this.getID(),validator); }

    private record GachaMachineMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new GachaMachineMenu(containerId,playerInventory,this.traderID,this.validator); }
    }

    @Override
    protected TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        GachaStorageNode storageNode = this.assertNode(GachaStorageNode.TYPE);
        MoneyStorageNode moneyNode = this.assertNode(MoneyStorageNode.TYPE);
        GachaNode node = this.assertNode(GachaNode.TYPE);

        GachaDummyTrade trade = node.getTrade(tradeIndex);
        if(trade == null || !node.getPrice().isValidPrice())
            return TradeResult.FAIL_INVALID_TRADE;

        if(storageNode.getStorage().isEmpty())
            return TradeResult.FAIL_OUT_OF_STOCK;

        //Check if the player is allowed to do the trade
        if(this.runPreTradeEvent(trade, context).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        MoneyValue cost = trade.getCost(context);
        if(!context.hasFunds(cost))
            return TradeResult.FAIL_CANNOT_AFFORD;

        //Check if they can hold the item
        ItemStack result = storageNode.getStorage().findRandomItem(!this.hasInfiniteStock());
        ItemStack gachaBall = GachaBallItem.createWithItem(result);
        if(!context.canFitItem(gachaBall) || !context.getPayment(cost))
        {
            //Put the item back into storage (unless we're creative as we didn't actually remove it)
            if(!this.hasInfiniteStock())
                storageNode.getStorage().forceInsertItem(result);
            return TradeResult.FAIL_NO_OUTPUT_SPACE;
        }

        //Actually take the money
        if(!context.getPayment(cost))
        {
            //Put the item back into storage (unless we're creative as we didn't actually remove it)
            if(!this.hasInfiniteStock())
                storageNode.getStorage().forceInsertItem(result);
            return TradeResult.FAIL_CANNOT_AFFORD;
        }

        //Give the player the item
        if(!context.putItem(gachaBall))
        {
            //Failed to give the customer the item, so give a refund and put the reward back into storage
            context.givePayment(cost);
            if(!this.hasInfiniteStock())
                storageNode.getStorage().forceInsertItem(result);
            return TradeResult.FAIL_NO_OUTPUT_SPACE;
        }

        MoneyValue taxesPaid = MoneyValue.empty();
        if(this.shouldStoreMoney())
            taxesPaid = moneyNode.addStoredMoney(cost,context.getTaxContext());

        //Push Notification
        this.pushNotification(GachaTradeNotification.create(result,cost,context.getPlayerReference(),this.getNotificationCategory(),taxesPaid));

        //Push the post-trade event
        return this.runPostTradeEvent(trade, context, cost, taxesPaid, gachaBall);
    }

}

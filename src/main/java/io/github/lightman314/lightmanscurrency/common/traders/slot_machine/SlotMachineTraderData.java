package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.templates.PersistentSupportingTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemStorageSource;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.common.menus.slot_machine.ResultHolder;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.menus.slot_machine.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.SlotMachineTradeNotification;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineDummyTrade;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlotMachineTraderData extends PersistentSupportingTraderData implements IItemStorageSource {

    public static final TraderType<SlotMachineTraderData> TYPE = TraderType.simple(SlotMachineTraderData::new,SlotMachineTraderData::new);

    TraderItemHandler<SlotMachineTraderData> itemHandler = new TraderItemHandler<>(this);

    public IItemHandler getItemHandler(Direction relativeSide) { return this.itemHandler.getHandler(relativeSide); }
    
    public final TraderItemStorage getStorage() { return this.findNodeValue(ItemStorageNode.TYPE,ItemStorageNode::getStorage); }

    private SlotMachineTraderData() { super(); }
    public SlotMachineTraderData(Level level, BlockPos pos) { super(false,level, pos); }
    private SlotMachineTraderData(long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    @Override
    public TraderType<?> getType() { return TYPE; }
    @Override
    protected void addCustomNodes(NodeCollector collector) {
        collector.addNode(UpgradesNode.TYPE,5);
        collector.addNode(InputNode.TYPE);
        collector.addNode(ItemStorageNode.TYPE);
        collector.addNode(SlotMachineNode.TYPE);
    }

    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADER_ALT; }

    @Override
    protected MenuProvider getTraderMenuProvider(MenuValidator validator) { return new SlotMachineMenuProvider(this.getID(), validator); }

    private record SlotMachineMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {

        @Override
        public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new SlotMachineMenu(windowID, inventory, this.traderID, this.validator); }

    }

    @Override
    public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        SlotMachineNode slotsNode = this.assertNode(SlotMachineNode.TYPE);
        ItemStorageNode storageNode = this.assertNode(ItemStorageNode.TYPE);
        MoneyStorageNode moneyNode = this.assertNode(MoneyStorageNode.TYPE);

        if(!this.hasValidTrade())
            return TradeResult.FAIL_INVALID_TRADE;

        SlotMachineDummyTrade trade = slotsNode.getTrade(0);
        if(trade == null)
        {
            LightmansCurrency.LogError("Slot Machine somehow doesn't have a valid trade!");
            return TradeResult.FAIL_INVALID_TRADE;
        }

        if(!context.hasPlayerReference())
            return TradeResult.FAIL_NULL;

        if(!trade.hasStock(context))
            return TradeResult.FAIL_OUT_OF_STOCK;

        //Check if the player is allowed to do the trade
        if(this.runPreTradeEvent(trade, context).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        //Get the cost of the trade
        MoneyValue price = this.runTradeCostEvent(trade, context).getCostResult();

        //Get the Result Items
        @Nullable
        SlotMachineEntry loot = slotsNode.getRandomizedEntry(context);

        //Confirm that the customer can hold the rewards
        if(loot != null && !loot.CanGiveToCustomer(context))
            return TradeResult.FAIL_NO_OUTPUT_SPACE;

        //Accept the payment
        if(context.getPayment(price))
        {
            if(loot != null && !loot.GiveToCustomer(this, context))
            {
                //Refund the money taken
                context.givePayment(price);
                return TradeResult.FAIL_NO_OUTPUT_SPACE;
            }

            List<IconData> newIcons = loot == null ? SlotMachineEntry.createDefaultIcons() : loot.getIconsToDisplay();
            slotsNode.setLastIcons(newIcons);

            //Give the result holder the updated icons
            if(context.getCustomData(ResultHolder.CONTEXT_KEY) instanceof ResultHolder holder)
                holder.setIcons(ImmutableList.copyOf(slotsNode.getLastIcons()));

            MoneyValue taxesPaid = MoneyValue.empty();

            //Ignore editing internal storage if this is flagged as creative.
            if(!this.hasInfiniteStock())
            {
                //Give the paid cost to storage
                taxesPaid = moneyNode.addStoredMoney(price, context.getTaxContext());

                //Push out of stock notification
                if(!trade.hasStock(TradeContext.createStorageMode(this)))
                    this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), -1));
            }

            //Handle Stats
            this.incrementStat(StatKeys.Traders.MONEY_EARNED,price);
            if(!taxesPaid.isEmpty())
                this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);
            if(loot != null && loot.isMoney())
                this.incrementStat(StatKeys.Traders.MONEY_PAID, loot.getMoneyValue());

            //Push Notification
            this.pushNotification(SlotMachineTradeNotification.create(loot, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

            List<Object> product = new ArrayList<>();
            if(loot != null)
            {
                if(loot.isMoney())
                    product.add(loot.getMoneyValue());
                else
                    product.addAll(ItemHandlerUtil.copyList(loot.items));
            }

            //Push the post-trade event
            return this.runPostTradeEvent(trade,context,price,taxesPaid,product);
        }
        else
            return TradeResult.FAIL_CANNOT_AFFORD;
    }

}

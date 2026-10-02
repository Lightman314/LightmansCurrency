package io.github.lightman314.lightmanscurrency.features.trader.item.nodes;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ExtractionResults;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAdminSettings;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IInteractionSlotProvider;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot.CapabilitySlotData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.UpgradeableTradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.menu.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.notifications.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractItemTradesNode<T extends ItemTradeData> extends UpgradeableTradingNode<T> implements IInteractionSlotProvider {

    public static final Identifier TRADE_COUNT_ARG = LCApi.id("item_trade_count");

    protected AbstractItemTradesNode() { }
    protected AbstractItemTradesNode(int baseCount,int upgradeCount,List<T> trades) { super(baseCount,upgradeCount,trades); }

    @Nullable
    @Override
    public Identifier advancedEditTabKey() { return ItemTradeEditTab.KEY; }

    @Nullable
    @Override
    protected Identifier getBaseCountArgument() { return TRADE_COUNT_ARG; }

    @Override
    public Component getSettingsName() { return ItemTradesNode.NAME.get(); }
    @Override
    public Component getSetLabel() { return ItemTradesNode.NAME.get(); }

    @Override
    public TradeResult executeTrade(TradeContext context,int tradeIndex) throws TradeFailedException {
        T trade = this.getTrade(tradeIndex);
        if(trade == null)
            return TradeResult.FAIL_NULL;
        TradePrice price = trade.getPrice(context);
        if(!price.isValid(context.getTrader(),trade))
            return TradeResult.FAIL_INVALID_TRADE;
        TradeDirection direction = trade.getDirection();
        if(direction.isOther())
            return TradeResult.FAIL_INVALID_TRADE;
        //Transfer the trades price
        TradePrice.TransferResult result = price.transferPrice(direction,context);
        if(!result.isSuccess())
            return TradeResult.FAIL_INVALID_TRADE;
        boolean hadStock = trade.predictHasStock(context.getTrader(),context.getTransaction());
        //Transfer the items
        ResourceHandler<ItemResource> storage = context.getTraderResource(BuiltInResourceTypes.ITEM);
        ResourceHandler<ItemResource> customerItems = context.getCustomerResource(BuiltInResourceTypes.ITEM);
        //Allow the trade to implement the item transfer so that complex trade children like ticket trades can handle this independently
        List<ItemStack> product;
        if(direction.isSale())
            product = this.transferItemsFromTraderToCustomer(trade,storage,customerItems,context);
        else
            product = this.transferItemsFromCustomerToTrader(trade,storage,customerItems,context);
        Notification notification = this.buildNotification(trade,product,result.getReceipt(),result,context);
        if(notification != null) //Post the trade notification
            this.postNotification(notification);
        if(hadStock && !trade.predictHasStock(context.getTrader(),context.getTransaction()))
            this.postOutOfStockNotification(tradeIndex);
        return this.finishSuccessfulTrade(context,trade,price,result,ItemHelper.combineStacks(product),notification);
    }

    //i.e. Item Transfer for sale trades
    protected List<ItemStack> transferItemsFromTraderToCustomer(T trade,ResourceHandler<ItemResource> traderStorage,ResourceHandler<ItemResource> customerStorage,TradeContext context) throws TradeFailedException {
        List<TradeItem> items = trade.getCombinedItems();
        if(items.isEmpty())
            throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
        List<ItemStack> product = new ArrayList<>();
        TraderAdminSettings settings = context.getAdminSettings();
        for(TradeItem item : items)
        {
            //Extract the items from the trader
            try(Transaction extractTransaction = Transaction.open(context.getTransaction())) {
                ExtractionResults<ItemResource> transferResult = ResourceHelper.extractRandomToTarget(traderStorage,item,item.getCount(),context.getRandom(),extractTransaction);
                //Special handling of the storage extraction if the trader has infinite stock,
                // and doesn't have enough items in storage to randomize the "normal" way
                if(transferResult.totalCount() != item.getCount() && settings.hasInfiniteStock()) {
                    transferResult = new ExtractionResults<>(item.extractFromUnlimitedResources(traderStorage,context.getRandom(),extractTransaction));
                }
                //Cancel the trader item extract **only if** the trader has infinite stock
                if(settings.hasInfiniteStock())
                    extractTransaction.close();
                else
                    extractTransaction.commit();
                if(transferResult.totalCount() == item.getCount())
                {
                    //Now give the items to the customer
                    for(ResourceStack<ItemResource> taken : transferResult.extracted())
                    {
                        int inserted = ResourceHandlerUtil.insertStacking(customerStorage,taken.resource(),taken.amount(),context.getTransaction());
                        if(inserted != taken.amount())
                            throw new TradeFailedException(TradeResult.FAIL_NO_OUTPUT_SPACE);
                        //If the product was successfully transferred, update the product context
                        product.add(taken.resource().toStack(taken.amount()));
                    }
                }
                else
                    throw new TradeFailedException(TradeResult.FAIL_OUT_OF_STOCK);
            }
        }
        return product;
    }

    //Item Transfer for purchase trades
    protected List<ItemStack> transferItemsFromCustomerToTrader(T trade,ResourceHandler<ItemResource> traderStorage,ResourceHandler<ItemResource> customerStorage,TradeContext context) throws TradeFailedException {
        List<TradeItem> items = trade.getCombinedItems();
        if(items.isEmpty())
            throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
        List<ItemStack> product = new ArrayList<>();
        TraderAdminSettings settings = context.getAdminSettings();
        for(TradeItem item : items)
        {
            //Extract the items from the customer
            //Use extract first so that the customer isn't surprised by the trader taking unexpected items
            //Also allows us to properly highlight the items that will be taken from their inventory since the action is predictable
            ExtractionResults<ItemResource> transferResult = ResourceHelper.extractFirstToTarget(customerStorage,item,item.getCount(),context.getTransaction());
            if(transferResult.totalCount() == item.getCount())
            {
                if(settings.shouldStorePrice()) {
                    //Now insert them into the trader
                    for(ResourceStack<ItemResource> taken : transferResult.extracted())
                    {
                        int inserted = traderStorage.insert(taken.resource(),taken.amount(),context.getTransaction());
                        if(inserted != taken.amount() && !settings.hasInfiniteStock()) {
                            //Do NOT fail the trade when failing to insert items in the trader **if** the trade also has infinite stock
                            throw new TradeFailedException(TradeResult.FAIL_NO_INPUT_SPACE);
                        }
                        //If the product was successfully transferred, update the product context
                        product.add(taken.resource().toStack(taken.amount()));
                    }
                }
            }
            else
                throw new TradeFailedException(TradeResult.FAIL_CANNOT_AFFORD);
        }
        return product;
    }

    @Nullable
    protected Notification buildNotification(T trade,List<ItemStack> product,TradePriceReceipt price,TradePrice.TransferResult priceTransfer,TradeContext context) {
        return new ItemTradeNotification(trade.getDirection(),price,ItemHelper.copyList(product),context.getCustomer().getName(this),new TraderCategory(context.getTrader()),priceTransfer.getTaxesPaid());
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        super.addTabs(builder);
        builder.addTab(ItemTradeEditTab::new);
    }

    @Override
    public void addInteractionSlot(Consumer<InteractionSlotData> builder) { builder.accept(CapabilitySlotData.ITEMS); }

}
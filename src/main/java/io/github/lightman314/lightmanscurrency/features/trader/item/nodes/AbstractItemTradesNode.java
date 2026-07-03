package io.github.lightman314.lightmanscurrency.features.trader.item.nodes;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.menu.AdvancedItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractItemTradesNode<T extends ItemTradeData> extends TradingNode<T> {

    protected int baseCount = 1;
    protected int upgradeCount = 0;
    private final List<T> trades = new ArrayList<>();

    protected AbstractItemTradesNode() {}
    protected AbstractItemTradesNode(int count, int upgradeCount, List<T> trades)
    {
        this.baseCount = count;
        this.upgradeCount = upgradeCount;
        this.trades.addAll(trades);
        this.attachTrades();
    }

    @Override
    public void updateArgument(Optional<Object> argument) {
        if(argument.isPresent() && argument.get() instanceof Number n)
            this.baseCount = Math.clamp(n.intValue(),1,TraderData.GLOBAL_TRADE_LIMIT);
    }

    @Override
    public void onAttach() {
        //Validate the total trade count
        int totalCount = this.baseCount + this.upgradeCount;
        this.forceTradeCount(totalCount);
    }

    @Override
    protected List<T> getMutableTrades() { return this.trades; }

    @Override
    public Component getSetLabel() {
        //TODO translate
        return Component.literal("Item Trades");
    }

    @Override
    public TradeResult executeTrade(TradeContext context, int tradeIndex) throws TradeFailedException {
        ItemTradeData trade = this.getTrade(tradeIndex);
        if(trade == null)
            return TradeResult.FAIL_NULL;
        List<TradeItem> items = trade.getCombinedItems();
        if(items.isEmpty()) //Fail if items aren't defined
            return TradeResult.FAIL_INVALID_TRADE;
        TradePrice price = trade.getPrice(context);
        if(!price.isValid(this.trader,trade))
            return TradeResult.FAIL_INVALID_TRADE;
        TradeDirection direction = trade.getDirection();
        if(direction.isOther())
            return TradeResult.FAIL_INVALID_TRADE;
        //Transfer the trades price
        TradePrice.TransferResult result = price.transferPrice(direction,context);
        if(!result.isSuccess())
            return TradeResult.FAIL_INVALID_TRADE;
        //Transfer the items
        ResourceHandler<ItemResource> fromItems = context.getResource(ResourceSource.fromResource(direction),BuiltInResourceTypes.ITEM);
        ResourceHandler<ItemResource> toItems = context.getResource(ResourceSource.toResource(direction),BuiltInResourceTypes.ITEM);
        List<ItemStack> product = new ArrayList<>();
        for(TradeItem item : items)
        {
            //Extract the items from the desired container
            ResourceHelper.ExtractionResults<ItemResource> transferResult = ResourceHelper.extractRandomToTarget(fromItems,item,item.getCount(),context.getRandom(),context.getTransaction());
            if(transferResult.totalCount() == item.getCount())
            {
                //Now insert them into the desired target
                for(ResourceStack<ItemResource> taken : transferResult.extracted())
                {
                    int inserted = toItems.insert(taken.resource(),taken.amount(),context.getTransaction());
                    if(inserted != item.getCount())
                        return direction.isSale() ? TradeResult.FAIL_NO_OUTPUT_SPACE : TradeResult.FAIL_NO_INPUT_SPACE;
                    //If the product was successfully transferred, update the product context
                    product.add(taken.resource().toStack(taken.amount()));
                }
            }
            else
                return direction.isSale() ? TradeResult.FAIL_OUT_OF_STOCK : TradeResult.FAIL_CANNOT_AFFORD;
        }
        return this.finishSuccessfulTrade(context,trade,price,result,ItemHelper.combineStacks(product));
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        super.addTabs(builder);
        builder.addTab(AdvancedItemTradeEditTab::new);
    }

}
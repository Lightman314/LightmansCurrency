package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.ExtractionResults;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IItemStorageFilter;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemPrice extends TradePrice implements IItemStorageFilter {

    public static final MapCodec<ItemPrice> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradeItem.CODEC.listOf().fieldOf("items").forGetter(p -> p.items)
    ).apply(builder,ItemPrice::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemPrice> STREAM_CODEC = TradeItem.STREAM_CODEC.apply(ByteBufCodecs.list(2))
            .map(ItemPrice::new,p -> p.items);

    public static final TradePriceType<ItemPrice> TYPE = new TradePriceType<>(MAP_CODEC,STREAM_CODEC,ItemPrice::new);

    public static final TextEntry OPTION_NAME = TextEntry.priceOption(ItemPrice.TYPE);

    private final ImmutableList<TradeItem> items = TradeItem.createList(2);
    private boolean invalidSlot(int slot) { return slot < 0 || slot >= 2; }
    public TradeItem getItem(int slot) {
        if(this.invalidSlot(slot))
            return TradeItem.create();
        return this.items.get(slot);
    }

    private ItemPrice() {}
    private ItemPrice(List<TradeItem> items) { TradeItem.loadList(this.items,items); }

    @Override
    public TradePriceType<?> getType() { return TYPE; }

    @Override
    public TradePrice copy() { return new ItemPrice(ListHelper.copyList(this.items,TradeItem::copy)); }

    @Override
    public Set<TradeDirection> supportedTradeTypes() { return SALE_ONLY_SET; }

    @Override
    public boolean maySupportTrade(TraderData trader, TradeData trade) { return trader.providesResources(BuiltInResourceTypes.ITEM) && !trader.hasNode(LCTags.TraderNodes.DENY_ITEM_BARTERING); }

    @Override
    protected boolean isValid() { return this.items.stream().anyMatch(item -> !item.isEmpty()); }

    /**
     * @see io.github.lightman314.lightmanscurrency.features.trader.item.nodes.AbstractItemTradesNode#transferItemsFromCustomerToTrader(ItemTradeData, ResourceHandler, ResourceHandler, TradeContext) Item Trade Equivalent
     */
    @Override
    public TransferResult transferFromCustomerToTrader(TradeContext context) throws TradeFailedException {
        List<TradeItem> items = TradeItem.combineMatching(this.items);
        if(items.isEmpty())
            throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
        List<ItemStack> product = new ArrayList<>();
        ResourceHandler<ItemResource> customerStorage = context.getCustomerResource(BuiltInResourceTypes.ITEM);
        ResourceHandler<ItemResource> traderStorage = context.getTraderResource(BuiltInResourceTypes.ITEM);
        for(TradeItem item : items)
        {
            //Extract the items from the customer
            //Use extract first so that the customer isn't surprised by the trader taking unexpected items
            //Also allows us to properly highlight the items that will be taken from their inventory since the action is predictable
            ExtractionResults<ItemResource> transferResult = ResourceHelper.extractFirstToTarget(customerStorage,item,item.getCount(),context.getTransaction());
            if(transferResult.totalCount() == item.getCount())
            {
                if(this.shouldStoreInTrader(context)) {
                    //Now insert them into the trader
                    for(ResourceStack<ItemResource> taken : transferResult.extracted())
                    {
                        int inserted = traderStorage.insert(taken.resource(),taken.amount(),context.getTransaction());
                        if(inserted != taken.amount() && !this.hasInfiniteStock(context)) {
                            //Do NOT fail the trade when failing to insert items in the trader **if** the trade also has infinite stock
                            return TransferResult.FAILED_TO_TAKE;
                        }
                        //If the product was successfully transferred, update the product context
                        product.add(taken.resource().toStack(taken.amount()));
                    }
                }
            }
            else
                throw new TradeFailedException(TradeResult.FAIL_CANNOT_AFFORD);
        }
        return TransferResult.success(new ItemReceipt(product));
    }

    @Override
    public TransferResult transferFromTraderToCustomer(TradeContext context) {
        //We do not support Item Price transfers from trader to customer
        return TransferResult.FAILED_TO_TAKE;
    }

    @Override
    public long getAvailableStock(TradeContext context) {
        if(IAdminSettingProvider.hasInfiniteStock(context.getTrader()))
            return Long.MAX_VALUE;
        List<TradeItem> combinedItems = TradeItem.combineMatching(this.items);
        if(combinedItems.isEmpty())
            return 0;
        long minStock = Long.MAX_VALUE;
        ResourceHandler<ItemResource> itemHandler = context.getTraderResource(BuiltInResourceTypes.ITEM);
        try(Transaction tx = Transaction.open(context.getTransaction()))
        {
            for(TradeItem requirement : combinedItems)
            {
                ExtractionResults<ItemResource> results = ResourceHelper.extractFirstToTarget(itemHandler,requirement,Integer.MAX_VALUE,tx);
                long rStock = results.totalCount() / requirement.getCount();
                minStock = Math.min(rStock,minStock);
            }
        }
        return minStock;
    }

    @Override
    public boolean showOutOfSpaceWarning(TradeContext context) {
        List<TradeItem> items = TradeItem.combineMatching(this.items);
        if(items.isEmpty())
            return false;
        try(Transaction tx = Transaction.open(context.getTransaction())) {
            ResourceHandler<ItemResource> customerItems = context.getCustomerResource(BuiltInResourceTypes.ITEM);
            List<ResourceStack<ItemResource>> queryItems = new ArrayList<>();
            for(TradeItem item : items) {
                ExtractionResults<ItemResource> results = ResourceHelper.extractFirstToTarget(customerItems,item,item.getCount(),tx);
                if(results.totalCount() != item.getCount()) {
                    //Extract the results manually
                    queryItems.add(item.getDummyStack());
                }
                else
                    queryItems.addAll(results.extracted());
            }
            queryItems = ResourceHelper.mergeResources(queryItems);
            ResourceHandler<ItemResource> traderItems = context.getTraderResource(BuiltInResourceTypes.ITEM);
            for(ResourceStack<ItemResource> stack : queryItems) {
                int inserted = traderItems.insert(stack.resource(),stack.amount(),tx);
                if(inserted != stack.amount())
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean showCannotAffordWarning(TradeContext context) {
        List<TradeItem> items = TradeItem.combineMatching(this.items);
        if(items.isEmpty())
            return false;
        try(Transaction tx = Transaction.open(context.getTransaction())) {
            ResourceHandler<ItemResource> customerItems = context.getCustomerResource(BuiltInResourceTypes.ITEM);
            for(TradeItem item : items) {
                ExtractionResults<ItemResource> results = ResourceHelper.extractFirstToTarget(customerItems,item,item.getCount(),tx);
                if(results.totalCount() != item.getCount())
                    return true;
            }
        }
        return false;
    }

    @Override
    public TradePrice percentageOfValue(int percentage) { return this; }

    @Override
    public boolean onClickInteraction(Player player,TradeData trade,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        int itemSlot = slot.slot();
        if(this.invalidSlot(itemSlot))
            return false;
        //Selection interactions
        TradeItem item = this.getItem(itemSlot);
        if(context.hasShiftDown() || (item.isEmpty() && heldItem.isEmpty()))
        {
            if(context.handler().isSimpleEdit())
            {
                //Open the advanced edit menu
                context.handler().openAdvancedEdit(trade,slot);
            }
            if(context.handler().isAdvancedEdit() && !context.handler().isSelected(slot))
            {
                //If this item slot isn't currently selected, change to the selection
                context.handler().changeSelection(slot);
            }
            return true;
        }
        //Normal item interactions
        if(item.isEmpty() || !ItemStack.isSameItemSameComponents(item.getStack(),heldItem))
        {
            //Define the item
            ItemStack newStack = heldItem.copy();
            if(button == 1)
                newStack.setCount(1);
            item.setStack(newStack);
        }
        else
        {
            if(button == 0) //Completely override the count if it's a left-click
                item.setCount(heldItem.getCount());
            else
                item.grow(1);
        }
        this.setChanged();
        return true;
    }

    @Override
    public boolean onScrollInteraction(Player player,TradeData trade,TradeSlot slot, float scroll, ItemStack heldItem, TradeEditContext context) {
        if(context.handler().isAdvancedEdit())
        {
            int itemSlot = slot.slot();
            TradeItem item = this.getItem(itemSlot);
            if(!item.isEmpty())
            {
                if(scroll > 0 && item.grow(1))
                {
                    this.setChanged();
                    return true;
                }
                if(scroll < 0 && item.shrink(1))
                {
                    this.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void handleCustomEditMessage(FancyPacketMap packet,TradeSlot slot) {
        if(packet.contains("setItem"))
        {
            TradeItem item = this.getItem(slot.slot());
            item.setStack(packet.getItem("setItem",true));
            this.setChanged();
        }
        if(packet.contains("setStrict")) {
            TradeItem item = this.getItem(slot.slot());
            item.setStrict(packet.getBoolean("setStrict"));
            this.setChanged();
        }
    }

    @Override
    public Component getNotificationText() {
        return ItemHelper.formatItemNames(this.items.stream().map(TradeItem::getStack).toList());
    }

    @Override
    public String toString() { return "ItemPrice[" + this.items.getFirst() + "," + this.items.getLast() + "]"; }

    @Override
    public boolean itemAllowedInStorage(ItemResource item) { return TradeItem.allowedInStorage(this.items,item); }

}
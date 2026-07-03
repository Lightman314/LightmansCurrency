package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminRuleProvider;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Consumer;

public class ItemPrice extends TradePrice {

    public static final MapCodec<ItemPrice> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradeItem.CODEC.listOf().fieldOf("items").forGetter(p -> p.items)
    ).apply(builder,ItemPrice::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemPrice> STREAM_CODEC = TradeItem.STREAM_CODEC.apply(ByteBufCodecs.list(2))
            .map(ItemPrice::new,p -> p.items);

    public static final TradePriceType<ItemPrice> TYPE = new TradePriceType<>(MAP_CODEC,STREAM_CODEC,ItemPrice::new);

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
    public boolean supportsTrade(TraderData trader, TradeData trade) { return trader.providesResources(BuiltInResourceTypes.ITEM); }

    @Override
    protected boolean isValid() { return this.items.stream().anyMatch(item -> !item.isEmpty()); }

    @Override
    public TransferResult transferFromCustomerToTrader(TradeContext context) {
        return null;
    }

    @Override
    public TransferResult transferFromTraderToCustomer(TradeContext context) {
        return null;
    }

    @Override
    public long getAvailableStock(TradeContext context) {
        if(IAdminRuleProvider.hasInfiniteStock(context.getTrader()))
            return Long.MAX_VALUE;
        List<TradeItem> combinedItems = TradeItem.combineMatching(this.items);
        if(combinedItems.isEmpty())
            return 0;
        long minStock = Long.MAX_VALUE;
        ResourceHandler<ItemResource> itemHandler = context.getResource(ResourceSource.TRADER,BuiltInResourceTypes.ITEM);
        try(Transaction tx = Transaction.open(context.getTransaction()))
        {
            for(TradeItem requirement : combinedItems)
            {
                ResourceHelper.ExtractionResults<ItemResource> results = ResourceHelper.extractFirstToTarget(itemHandler,requirement,Integer.MAX_VALUE,tx);
                long rStock = results.totalCount() / requirement.getCount();
                minStock = Math.min(rStock,minStock);
            }
        }
        return minStock;
    }

    @Override
    public TradePrice percentageOfValue(int percentage) { return this; }

    @Override
    public boolean onClickInteraction(Player player,TradeData trade,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        int itemSlot = slot.slot();
        if(this.invalidSlot(itemSlot))
            return false;
        //Special shift interactions
        if(context.hasShiftDown())
        {
            if(context.handler().isSimpleEdit())
            {
                //Open the advanced edit menu
                context.handler().openAdvancedEdit(trade,slot);
                return true;
            }
            if(context.handler().isAdvancedEdit() && !context.handler().isSelected(slot))
            {
                //If this item slot isn't currently selected, change to the selection
                context.handler().changeSelection(slot);
                return true;
            }
        }
        //Normal item interactions
        TradeItem item = this.getItem(itemSlot);
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

    public Consumer<ItemStack> setItemPacket(ITradeInteractionHandler handler) {

    }

    @Override
    public void handleCustomEditMessage(FancyPacketMap packet) {
        if(packet.contains("setItem"))
        {

        }
    }

}
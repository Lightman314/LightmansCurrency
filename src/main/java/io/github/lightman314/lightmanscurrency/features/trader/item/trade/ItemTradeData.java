package io.github.lightman314.lightmanscurrency.features.trader.item.trade;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class ItemTradeData extends TradeData {

    private static final MapCodec<ItemTradeData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradePrice.CODEC.fieldOf("price").forGetter(ItemTradeData::getPrice),
            TradeItem.CODEC.listOf().fieldOf("items").forGetter(t -> t.sellItems)
    ).apply(builder,ItemTradeData::new));
    public static final Codec<ItemTradeData> CODEC = MAP_CODEC.codec();

    public static final TradeDataType<ItemTradeData> TYPE = new TradeDataType<>(MAP_CODEC);

    @Override
    public TradeDataType<?> getType() { return TYPE; }

    public ItemTradeData() {}
    protected ItemTradeData(TradePrice price,List<TradeItem> items) {
        this.setPrice(price);
        TradeItem.loadList(this.sellItems,items);
    }

    private TradeDirection type = TradeDirection.SALE;
    @Override
    public TradeDirection getDirection() { return this.type; }
    public void setType(TradeDirection type) {
        if(type.isOther() || type == this.type)
            return;
        this.type = type;
        this.setChanged(builder -> builder.setEnum("trade_type",this.type));
    }

    private final ImmutableList<TradeItem> sellItems = TradeItem.createList(2);
    private boolean invalidSlot(int slot) { return slot < 0 || slot >= 2; }
    public TradeItem getItem(int slot) {
        if(this.invalidSlot(slot))
            return TradeItem.create();
        return this.sellItems.get(slot);
    }
    public List<TradeItem> getCombinedItems() {
        return TradeItem.combineMatching(this.sellItems);
    }
    public void setItem(int slot,ItemStack item) {
        if(this.invalidSlot(slot))
            return;
       this.sellItems.get(slot).setStack(item);
        this.setItemChanged(slot);
    }
    public void growItem(int slot) {
        TradeItem item = this.getItem(slot);
        if(item.grow(1))
            this.setItemChanged(slot);
    }

    public final void setItemChanged(int slot) {
        this.setChanged(builder -> builder.set("item_" + slot,LCFancyPacketTypes.ITEM_REQUIREMENT,this.getItem(slot)));
    }

    @Override
    public void getFullPacket(FancyPacketMap.Mutable packet, ISyncingContext context) {
        packet.setEnum("trade_type",this.type);
    }

    @Override
    public void handlePacket(FancyPacketMap packet) {
        if(packet.contains("trade_type"))
            this.type = packet.getEnum("trade_type",TradeDirection.class);

    }

    @Override
    public long getStock(TradeContext context) {
        if(this.getDirection().isPurchase())
            return this.getPrice().getAvailableStock(context);
        //Calculate the number of items that can be obtained from the trade
        try(Transaction tx = Transaction.open(context.getTransaction()))
        {
            ResourceHandler<ItemResource> storage = context.getResource(ResourceSource.TRADER,BuiltInResourceTypes.ITEM);
            //TODO calculate the stock based on the available items in storage
        }
        return 0;
    }

    @Override
    public boolean processTradeClick(Player player,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        if(slot.isPriceSlot(this.type))
            return this.getPrice().onClickInteraction(player,this,slot,button,heldItem,context);
        int itemSlot = slot.slot();
        if(this.invalidSlot(itemSlot))
            return false;
        //Special shift interactions
        if(context.hasShiftDown())
        {
            if(context.handler().isSimpleEdit())
            {
                //Open the advanced edit menu
                context.handler().openAdvancedEdit(this,slot);
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
            else //Otherwise
                item.grow(1);
        }
        this.setItemChanged(itemSlot);
        return true;
    }

    @Override
    public boolean processTradeScroll(Player player,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context) {
        if(slot.isPriceSlot(this.type))
            return this.getPrice().onScrollInteraction(player,this,slot,deltaY,heldItem,context);
        else if(context.handler().isAdvancedEdit())
        {
            int itemSlot = slot.slot();
            TradeItem stack = this.getItem(itemSlot);
            if(!stack.isEmpty())
            {
                if(deltaY > 0 && stack.grow(1))
                {
                    this.setItemChanged(itemSlot);
                    return true;
                }
                if(deltaY < 0 && stack.shrink(1))
                {
                    this.setItemChanged(itemSlot);
                    return true;
                }
            }
        }
        return false;
    }

}

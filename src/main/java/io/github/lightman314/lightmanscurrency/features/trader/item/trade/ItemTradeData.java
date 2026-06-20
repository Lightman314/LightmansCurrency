package io.github.lightman314.lightmanscurrency.features.trader.item.trade;

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
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class ItemTradeData extends TradeData {

    private static final MapCodec<ItemTradeData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradePrice.CODEC.fieldOf("price").forGetter(ItemTradeData::getPrice),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(t -> t.sellItems)
    ).apply(builder,ItemTradeData::new));
    public static final Codec<ItemTradeData> CODEC = MAP_CODEC.codec();

    public static final TradeDataType<ItemTradeData> TYPE = new TradeDataType<>(MAP_CODEC);

    @Override
    public TradeDataType<?> getType() { return TYPE; }

    public ItemTradeData() {}
    protected ItemTradeData(TradePrice price,List<ItemStack> items) {
        this.setPrice(price);
        for(int i = 0; i < items.size() && i < this.sellItems.size(); ++i)
            this.sellItems.set(i,items.get(i).copy());
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

    private final NonNullList<ItemStack> sellItems = NonNullList.withSize(2,ItemStack.EMPTY);
    private boolean invalidSlot(int slot) { return slot < 0 || slot >= 2; }
    public ItemStack getItem(int slot) {
        if(this.invalidSlot(slot))
            return ItemStack.EMPTY;
        return this.sellItems.get(slot);
    }
    public void setItem(int slot,ItemStack item) {
        if(this.invalidSlot(slot))
            return;
        this.sellItems.set(slot,item.copy());
        this.setItemChanged(slot);
    }
    public void growItem(int slot) {
        ItemStack item = this.getItem(slot);
        if(!item.isEmpty())
        {
            item.grow(1);
            this.setItemChanged(slot);
        }
    }

    public final void setItemChanged(int slot) {
        this.setChanged(builder -> builder.setItem("item_" + slot,this.getItem(slot)));
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
    public boolean processTradeClick(TradeSlotType type,int slot,int button,ItemStack heldItem,TradeEditContext context) {
        //if(type.isPriceSlot(this.type))
        //    this.getPrice().processTradeClick(slot,button,heldItem,context);
        return false;
    }

    @Override
    public boolean processTradeScroll(TradeSlotType type,int slot,float deltaY,ItemStack heldItem,TradeEditContext context) {
        return false;
    }
}

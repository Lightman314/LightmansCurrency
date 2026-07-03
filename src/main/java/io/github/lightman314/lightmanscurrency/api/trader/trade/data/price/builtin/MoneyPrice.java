package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class MoneyPrice extends TradePrice {

    private static final MapCodec<MoneyPrice> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MoneyValue.CODEC.fieldOf("price").forGetter(MoneyPrice::getPrice)
    ).apply(builder, MoneyPrice::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,MoneyPrice> STREAM_CODEC = MoneyValue.STREAM_CODEC.map(MoneyPrice::new,MoneyPrice::getPrice);

    public static final TradePriceType<MoneyPrice> TYPE = new TradePriceType<>(MAP_CODEC,STREAM_CODEC,MoneyPrice::empty);

    private MoneyValue price;
    public MoneyValue getPrice() { return this.price; }
    public void setPrice(MoneyValue newValue) { this.price = newValue; }
    public static MoneyPrice empty() { return new MoneyPrice(MoneyValue.empty()); }
    public MoneyPrice(MoneyValue price) { this.price = price; }

    @Override
    public TradePriceType<?> getType() { return TYPE; }

    @Override
    public boolean supportsTrade(TraderData trader,TradeData trade) { return true; }
    @Override
    public boolean isValid() { return this.price.isValidPrice(); }

    @Override
    public TransferResult transferFromCustomerToTrader(TradeContext context) {
        //If this is free, nothing to do
        if(this.isFree())
            return TransferResult.SUCCESS;
        try(Transaction tx = Transaction.open(context.getTransaction()))
        {
            //Take from the customer
            MoneyResourceHandler handler = context.getResource(ResourceSource.CUSTOMER,BuiltInResourceTypes.MONEY);
            MoneyValue taken = handler.extract(this.price,tx);
            if(!taken.equals(this.price))
                return TransferResult.FAILED_TO_TAKE;
            //TODO pay taxes
            MoneyValue taxesPaid = MoneyValue.empty();
            MoneyValue toStore = this.price;
            //If we shouldn't store the result in the trader, then we don't need to actually store the money into the trader
            if(!this.shouldStoreInTrader(context))
                return TransferResult.taxedSuccess(taxesPaid);
            handler = context.getResource(ResourceSource.TRADER,BuiltInResourceTypes.MONEY);
            MoneyValue inserted = handler.insert(toStore,tx);
            if(!inserted.equals(toStore))
                return TransferResult.FAILED_TO_GIVE;
            //Commit the sub-transaction
            tx.commit();
            return TransferResult.taxedSuccess(taxesPaid);
        }
    }

    @Override
    public TransferResult transferFromTraderToCustomer(TradeContext context) {
        //If this is free, nothing to do
        if(this.isFree())
            return TransferResult.SUCCESS;
        try(Transaction tx = Transaction.open(context.getTransaction()))
        {
            //Take from the trader
            //TODO pay taxes
            MoneyValue taxesPaid = MoneyValue.empty();
            MoneyValue toTake = this.price;
            if(!this.hasInfiniteStock(context))
            {
                MoneyResourceHandler handler = context.getResource(ResourceSource.TRADER,BuiltInResourceTypes.MONEY);
                MoneyValue taken = handler.extract(toTake,tx);
                if(!taken.equals(toTake))
                    return TransferResult.FAILED_TO_TAKE;
            }
            //Now give to the player
            MoneyResourceHandler handler = context.getResource(ResourceSource.CUSTOMER,BuiltInResourceTypes.MONEY);
            MoneyValue inserted = handler.insert(this.price,tx);
            if(!inserted.equals(this.price))
                return TransferResult.FAILED_TO_GIVE;
            //Commit the sub-transaction
            tx.commit();
            return TransferResult.taxedSuccess(taxesPaid);
        }
    }

    @Override
    public long getAvailableStock(TradeContext context) {
        if(!this.price.isValidPrice())
            return 0;
        if(this.isFree() || this.hasInfiniteStock(context))
            return Long.MAX_VALUE;
        MoneyResourceHandler handler = context.getResource(ResourceSource.TRADER,BuiltInResourceTypes.MONEY);
        MoneyValue available = handler.getResource(this.price.getKey());
        return available.getInternalValue() / this.price.getInternalValue();
    }

    @Override
    public boolean isFree() { return this.price.isFree(); }
    @Override
    public TradePrice percentageOfValue(int percentage) { return new MoneyPrice(this.price.percentageOfValue(percentage)); }

    @Override
    public void handleCustomEditMessage(FancyPacketMap packet) {
        if(packet.contains("setPrice"))
            this.price = packet.get("setPrice",LCFancyPacketTypes.MONEY);
    }

    @Override
    public boolean onClickInteraction(Player player,TradeData trade,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        if(trade.getHolder() != null && context.handler().isSimpleEdit())
        {
            context.handler().openAdvancedEdit(trade,slot);
            return true;
        }
        if(context.handler().isAdvancedEdit() && !context.handler().isSelected(slot))
        {
            context.handler().changeSelection(slot);
            return true;
        }
        return false;
    }

}
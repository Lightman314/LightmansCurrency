package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MoneyPrice extends TradePrice {

    private static final MapCodec<MoneyPrice> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MoneyValue.CODEC.fieldOf("price").forGetter(MoneyPrice::getPrice)
    ).apply(builder, MoneyPrice::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,MoneyPrice> STREAM_CODEC = MoneyValue.STREAM_CODEC.map(MoneyPrice::new,MoneyPrice::getPrice);

    public static final TradePriceType<MoneyPrice> TYPE = new TradePriceType<>(MAP_CODEC,STREAM_CODEC);

    private MoneyValue price;
    public MoneyValue getPrice() { return this.price; }
    public void setPrice(MoneyValue newValue) { this.price = newValue; }
    public MoneyPrice(MoneyValue price) { this.price = price; }

    @Override
    public TradePriceType<?> getType() { return TYPE; }

    @Override
    public boolean isValid(TradeDirection direction) { return this.price.isValidPrice(); }

    @Override
    protected boolean takeFromSource(ResourceSource source,TradeContext context) {
        if(!this.price.isValidPrice())
            return false;
        if(this.price.isFree())
            return true;
        MoneyResourceHandler handler = context.getResource(source,BuiltInResourceTypes.MONEY);
        //Taken successfully if the amount extracted is the total amount
        return handler.extract(this.price,context.getTransaction()).equals(this.price);
    }

    @Override
    protected boolean giveToSource(ResourceSource source,TradeContext context) {
        if(!this.price.isValidPrice())
            return false;
        if(this.price.isFree())
            return true;
        MoneyResourceHandler handler = context.getResource(source,BuiltInResourceTypes.MONEY);
        //Given successfully if the amount inserted is the total amount
        return handler.insert(this.price,context.getTransaction()).equals(this.price);
    }

    @Override
    public long getAvailableStock(TradeContext context) {
        if(!this.price.isValidPrice())
            return 0;
        MoneyResourceHandler handler = context.getResource(ResourceSource.TRADER,BuiltInResourceTypes.MONEY);
        return 0;
    }

    @Override
    public boolean isFree() { return this.price.isFree(); }
    @Override
    public TradePrice percentageOfValue(int percentage) { return new MoneyPrice(this.price.percentageOfValue(percentage)); }

}
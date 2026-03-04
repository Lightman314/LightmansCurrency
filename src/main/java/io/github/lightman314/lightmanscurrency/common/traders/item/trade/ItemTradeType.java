package io.github.lightman314.lightmanscurrency.common.traders.item.trade;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ItemTradeType<T extends ItemTradeData> {

    public abstract ItemTradeData create(boolean validateTrades);
    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    public final ItemTradeData validateType(ItemTradeData trade)
    {
        if(trade.getType() != this)
            return this.changeType(trade);
        return trade;
    }
    public abstract ItemTradeData changeType(ItemTradeData other);

    @Override
    public int hashCode() { return LCRegistries.ITEM_TRADE.getKey(this).hashCode(); }
    @Override
    public String toString() { return "ItemTradeType[" + LCRegistries.ITEM_TRADE.getKey(this) + "]"; }
}

package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceiptType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class MoneyReceipt extends TradePriceReceipt {

    private static final MapCodec<MoneyReceipt> MAP_CODEC = MoneyValue.CODEC.fieldOf("money").xmap(MoneyReceipt::new,MoneyReceipt::getMoney);
    private static final StreamCodec<RegistryFriendlyByteBuf,MoneyReceipt> STREAM_CODEC = MoneyValue.STREAM_CODEC.map(MoneyReceipt::new,MoneyReceipt::getMoney);

    public static final TradePriceReceiptType<MoneyReceipt> TYPE = new TradePriceReceiptType<>(MAP_CODEC,STREAM_CODEC);

    private final MoneyValue money;
    public final MoneyValue getMoney() { return this.money; }
    public MoneyReceipt(MoneyValue money) { this.money = money; }

    @Override
    public TradePriceReceiptType<?> getType() { return TYPE; }

    @Override
    public Component getText() { return this.money.getText(); }

    @Override
    protected boolean equals(TradePriceReceipt other) { return other instanceof MoneyReceipt r && r.money.equals(this.money); }

    @Override
    protected int hash() { return this.money.hashCode(); }

}

package io.github.lightman314.lightmanscurrency.api.money.values.source;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.source.builtin.DirectSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public abstract class MoneyValueSource {

    public static final Codec<MoneyValueSource> CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(
            LCRegistries.Money.VALUE_SOURCE.byNameCodec().dispatch(MoneyValueSource::getType,MoneyValueSourceType::codec),
            DirectSource.DIRECT_CODEC));
    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyValueSource> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Money.VALUE_SOURCE_KEY)
            .dispatch(MoneyValueSource::getType,MoneyValueSourceType::streamCodec);

    public abstract MoneyValue getMoneyValue();
    public abstract MoneyValueSourceType<?> getType();

    @Override
    public final int hashCode() { return Objects.hash(this.getType(),this.hash()); }

    protected abstract int hash();

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof MoneyValueSource source)
            return this.equals(source);
        return false;
    }

    protected abstract boolean equals(MoneyValueSource source);

}

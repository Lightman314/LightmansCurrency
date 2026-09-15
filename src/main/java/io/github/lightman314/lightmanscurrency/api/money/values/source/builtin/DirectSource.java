package io.github.lightman314.lightmanscurrency.api.money.values.source.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSource;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSourceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class DirectSource extends MoneyValueSource {

    private static final MapCodec<DirectSource> MAP_CODEC = MoneyValue.LENIENT_CODEC.fieldOf("value")
            .xmap(DirectSource::new,DirectSource::getMoneyValue);
    private static final StreamCodec<RegistryFriendlyByteBuf,DirectSource> STREAM_CODEC = MoneyValue.STREAM_CODEC
            .map(DirectSource::new,DirectSource::getMoneyValue);

    public static Codec<MoneyValueSource> DIRECT_CODEC = MAP_CODEC.codec().flatXmap(DataResult::success,
            source -> {
        if(source instanceof DirectSource ds)
            return DataResult.success(ds);
        return DataResult.error(() -> "Money Value Source is not direct!");
    });
    public static final MoneyValueSourceType<DirectSource> TYPE = new MoneyValueSourceType<>(MAP_CODEC,STREAM_CODEC);

    private final MoneyValue value;
    public DirectSource(MoneyValue value) { this.value = value; }

    @Override
    public MoneyValue getMoneyValue() { return this.value; }
    @Override
    public MoneyValueSourceType<?> getType() { return TYPE; }
    @Override
    protected int hash() { return this.value.hashCode(); }
    @Override
    protected boolean equals(MoneyValueSource source) { return source instanceof DirectSource s && s.value.equals(this.value); }

}

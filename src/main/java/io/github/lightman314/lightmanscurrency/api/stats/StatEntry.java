package io.github.lightman314.lightmanscurrency.api.stats;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class StatEntry<V,A> {

    public static final Codec<StatEntry<?,?>> CODEC = StatKey.MAP_CODEC.dispatch(StatEntry::getKey,StatEntry::getCodecForKey);
    public static final StreamCodec<RegistryFriendlyByteBuf,StatEntry<?,?>> STREAM_CODEC = StreamCodec.of((buf,entry) -> {
        StatKey.STREAM_CODEC.encode(buf,entry.key);
        entry.encodeValue(buf);
    },buf -> {
        StatKey<?,?> key = StatKey.STREAM_CODEC.decode(buf);
        return decodeUnsafe(key,key.type().streamCodec().decode(buf));
    });

    private static final Map<StatKey<?,?>,MapCodec<StatEntry<?,?>>> codecCache = new HashMap<>();

    private static <V,A> MapCodec<StatEntry<?,?>> getCodecForKey(StatKey<V,A> key) {
        if(!codecCache.containsKey(key)) {
            MapCodec<Either<V,Unit>> safeCodec = Codec.mapEither(key.type().codec(),Unit.CODEC.fieldOf("value"));
            codecCache.put(key,safeCodec.xmap(v -> decodeEitherUnsafe(key,v), e -> Either.left((V)e.getValue())));
        }
        return codecCache.get(key);
    }

    private final StatKey<V,A> key;
    private V value;
    public Identifier getID() { return this.key.key(); }
    public StatKey<V,A> getKey() { return this.key; }
    public StatKey<?,?> getAnonymousKey() { return this.key; }
    public StatType<V,A> getType() { return this.key.type(); }
    public StatEntry(StatKey<V,A> key) {
        this.key = key;
        this.value = this.getType().getEmptyValue();
    }
    private StatEntry(StatKey<V,A> key,V value) {
        this.key = key;
        this.value = value;
    }

    public V getValue() { return this.value; }

    public Component getName() { return this.key.getName(); }
    public Component getValueText() { return this.getType().getValueText(this.value); }
    @Nullable
    public List<Component> getValueTooltip() { return this.getType().getValueTooltip(this.value); }

    public void add(A addAmount) { this.value = this.getType().addToValue(this.value,addAmount); }
    public boolean reset() {
        StatType<V,A> type = this.getType();
        if(!type.isEmptyValue(this.value)) {
            this.value = type.getEmptyValue();
            return true;
        }
        return false;
    }

    public StatEntry<V,A> copy() { return new StatEntry<>(this.key,this.value); }

    private static <V,A> StatEntry<V,A> decodeEitherUnsafe(StatKey<V,A> key,Either<V,Unit> value) {
        return decodeUnsafe(key,value.map(UnaryOperator.identity(),UnaryOperator.identity()));
    }
    private static <V,A> StatEntry<V,A> decodeUnsafe(StatKey<V,A> key,Object value) {
        if(key.isValidValue(value))
            return new StatEntry<>(key,(V)value);
        LightmansCurrency.LogDebug(value.getClass().getName() + " is not a valid type for stat type " + key.type());
        return new StatEntry<>(key);
    }
    private void encodeValue(RegistryFriendlyByteBuf buf) { this.getType().streamCodec().encode(buf,this.value); }

}
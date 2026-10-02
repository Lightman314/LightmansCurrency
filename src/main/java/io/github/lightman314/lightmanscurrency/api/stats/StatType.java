package io.github.lightman314.lightmanscurrency.api.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.List;

public abstract class StatType<V,A> extends AbstractType.Serializable<V,StatType<?,?>> {

    protected StatType(Codec<V> codec,StreamCodec<? super RegistryFriendlyByteBuf,V> streamCodec) { this(codec.fieldOf("value"),streamCodec); }
    protected StatType(MapCodec<V> codec,StreamCodec<? super RegistryFriendlyByteBuf,V> streamCodec) {
        super(codec,streamCodec);
    }

    protected abstract Class<V> getValueClass();
    public final boolean isValidValue(Object value) { return this.getValueClass().isInstance(value); }

    public abstract V addToValue(V currentValue,A addition);
    public abstract V getEmptyValue();
    public abstract boolean isEmptyValue(V value);

    public abstract Component getValueText(V value);
    @Nullable
    public List<Component> getValueTooltip(V value) { return null; }

    @Override
    protected final Registry<StatType<?, ?>> getRegistry() { return LCRegistries.Data.STAT_TYPE; }
    @Override
    protected final String getName() { return "StatType"; }

    public static abstract class Singleton<T> extends StatType<T,T> {

        protected Singleton(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }
        protected Singleton(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }
    }

}
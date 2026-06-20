package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class PermissionType<T> {

    public abstract boolean allowedValue(T value);
    public abstract Codec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();
    public abstract T getEmpty();
    public abstract T getMaxValue();
    public abstract T getHighest(T value1,T value2);

    @Override
    public final int hashCode() { return RegistryHelper.hash(LCRegistries.Trader.PERMISSION_TYPE,this); }
    @Override
    public final String toString() { return RegistryHelper.toString("PermissionType",LCRegistries.Trader.PERMISSION_TYPE,this); }
}
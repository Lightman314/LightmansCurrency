package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class PermissionType<T> extends AbstractType<PermissionType<?>> {

    public abstract boolean allowedValue(T value);
    public abstract Codec<T> codec();

    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();
    public abstract T getEmpty();
    public abstract T getMaxValue();
    public abstract T getHighest(T value1,T value2);

    @Override
    protected final PermissionType<?> getEntry() { return super.getEntry(); }
    @Override
    protected final Registry<PermissionType<?>> getRegistry() { return LCRegistries.Trader.PERMISSION_TYPE; }
    @Override
    protected final String getName() { return "PermissionType"; }
}
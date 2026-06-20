package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class BooleanPermissionType extends PermissionType<Boolean> {

    public static final BooleanPermissionType INSTANCE = new BooleanPermissionType();
    private BooleanPermissionType() {}
    @Override
    public boolean allowedValue(Boolean value) { return value != null; }
    @Override
    public Codec<Boolean> codec() { return Codec.BOOL; }
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Boolean> streamCodec() { return ByteBufCodecs.BOOL; }
    @Override
    public Boolean getEmpty() { return false; }
    @Override
    public Boolean getMaxValue() { return true; }
    @Override
    public Boolean getHighest(Boolean value1, Boolean value2) { return value1 || value2; }

    public static Permission<Boolean> of(boolean defaultValue) { return new Permission<>(INSTANCE,() -> defaultValue); }

}
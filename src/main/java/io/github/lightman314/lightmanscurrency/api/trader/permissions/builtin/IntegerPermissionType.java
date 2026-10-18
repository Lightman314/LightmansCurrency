package io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class IntegerPermissionType extends PermissionType<Integer> {

    public static final IntegerPermissionType INSTANCE = new IntegerPermissionType();
    private IntegerPermissionType() {}
    @Override
    public boolean allowedValue(Integer value) { return value >= 0; }
    @Override
    public Codec<Integer> codec() { return Codec.INT; }
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Integer> streamCodec() { return ByteBufCodecs.INT; }
    @Override
    public Integer getEmpty() { return 0; }
    @Override
    public Integer getMaxValue() { return Integer.MAX_VALUE; }
    @Override
    public Integer getHighest(Integer value1, Integer value2) { return Math.max(value1,value2); }
    @Override
    public Class<Integer> getValueClass() { return Integer.class; }
    @Override
    public Component getValueText(Permission<Integer> permission,Integer value) { return Component.literal(NumberHelper.prettyInteger(value)); }

    public static Permission<Integer> of(int defaultValue, int maxValue)
    {
        if(maxValue <= 0)
            throw new IllegalArgumentException("Cannot have an integer permission with a max value <= 0");
        return new Permission<>(INSTANCE,() -> defaultValue,() -> maxValue,value -> value <= maxValue);
    }

}

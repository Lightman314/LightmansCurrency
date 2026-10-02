package io.github.lightman314.lightmanscurrency.api.stats.builtin;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;

public class IntegerStatType extends StatType.Singleton<Integer> {

    public static final StatType.Singleton<Integer> TYPE = new IntegerStatType();

    protected IntegerStatType() { super(Codec.intRange(0,Integer.MAX_VALUE),ByteBufCodecs.INT); }

    @Override
    protected Class<Integer> getValueClass() { return Integer.class; }
    @Override
    public Integer addToValue(Integer currentValue,Integer addition) { return currentValue + Math.max(addition,0); }
    @Override
    public Integer getEmptyValue() { return 0; }
    @Override
    public boolean isEmptyValue(Integer value) { return value == 0; }
    @Override
    public Component getValueText(Integer value) { return Component.literal(NumberHelper.prettyInteger(value)); }

}
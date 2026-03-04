package io.github.lightman314.lightmanscurrency.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class EnumUtil {
    
	public static <T extends Enum<?>> Codec<T> buildCodec(Class<T> clazz, String name)
	{
		return Codec.STRING.comapFlatMap(string -> {
			T result = enumFromString(string,clazz.getEnumConstants(),null);
			if(result == null)
				return DataResult.error(() -> string + " is not a valid " + name);
			return DataResult.success(result);
		},Enum::toString);
	}

    public static <T extends Enum<?>> StreamCodec<ByteBuf,T> streamCodec(Class<T> clazz,String name)
    {
        return ByteBufCodecs.INT.map(ordinal -> {
            T result = enumFromOrdinal(ordinal,clazz.getEnumConstants(),null);
            if(result == null)
                throw new IllegalArgumentException(ordinal + " is not a valid " + name + " ordinal");
            return result;
        },Enum::ordinal);
    }

    public static <T extends Enum<?>> T nextEnum(T value)
    {
        T[] allValues = (T[])value.getClass().getEnumConstants();
        return enumFromOrdinal(value.ordinal() + 1,allValues,allValues[0]);
    }

    public static <T extends Enum<?>> T previousEnum(T value)
    {
        T[] allValues = (T[])value.getClass().getEnumConstants();
        return enumFromOrdinal(value.ordinal() - 1, allValues, allValues[allValues.length - 1]);
    }

	public static <T extends Enum<?>> T enumFromOrdinal(int ordinal, T[] allValues, T defaultValue)
	{
		for(T val : allValues)
		{
			if(val.ordinal() == ordinal)
				return val;
		}
		return defaultValue;
	}

	public static <T extends Enum<?>> T enumFromString(String string, T[] allValues, T defaultValue)
	{
		for(T val : allValues)
		{
			if(val.toString().equalsIgnoreCase(string))
				return val;
		}
		return defaultValue;
	}
	
}

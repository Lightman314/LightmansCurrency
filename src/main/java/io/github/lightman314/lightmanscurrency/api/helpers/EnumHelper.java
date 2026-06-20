package io.github.lightman314.lightmanscurrency.api.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Locale;

public final class EnumHelper {

    private EnumHelper() {}

    public static <T extends Enum<T>> Codec<T> buildCodec(Class<T> clazz, String name)
    {
        return Codec.STRING.comapFlatMap(string -> {
            T result = enumFromString(string,clazz.getEnumConstants(),null);
            if(result == null)
                return DataResult.error(() -> "Could not parse '" + string + "' as a " + name);
            return DataResult.success(result);
        },T::toString);
    }

    public static <T extends Enum<T>> StreamCodec<ByteBuf,T> buildStreamCodec(Class<T> clazz, String name)
    {
        return ByteBufCodecs.INT.map(ordinal -> {
            T result = enumFromOrdinal(ordinal,clazz.getEnumConstants(),null);
            if(result == null)
                throw new DecoderException("Could not parse the " + name + " with an ordinal of " + ordinal);
            return result;
        },T::ordinal);
    }

    public static <T extends Enum<T>> T nextEnum(T value)
    {
        T[] allValues = (T[])value.getClass().getEnumConstants();
        return enumFromOrdinal(value.ordinal() + 1,allValues,allValues[0]);
    }

    public static <T extends Enum<T>> T previousEnum(T value)
    {
        T[] allValues = (T[])value.getClass().getEnumConstants();
        return enumFromOrdinal(value.ordinal() -1,allValues,allValues[allValues.length - 1]);
    }

    @Nullable
    public static <T extends Enum<T>> T enumFromOrdinal(int ordinal,T[] allValues,@Nullable T defaultValue)
    {
        for(T val : allValues)
        {
            if(val.ordinal() == ordinal)
                return val;
        }
        return defaultValue;
    }

    @Nullable
    public static <T extends Enum<T>> T enumFromString(String string,T[] allValues,@Nullable T defaultValue)
    {
        for(T val : allValues)
        {
            if(val.toString().equalsIgnoreCase(string))
                return val;
        }
        return defaultValue;
    }

    /**
     * Mirror of various {@link GsonHelper} methods, but for more easily obtaining enum values (with more helpful errors that include the enum names)
     */
    public static <T extends Enum<T>> T getAsEnum(JsonObject json,String entry,Class<T> clazz,String enumName) throws JsonSyntaxException
    {
        if(json.has(entry))
        {
            JsonElement e = json.get(entry);
            if(e instanceof JsonPrimitive primitive && primitive.isString())
            {
                String s = primitive.getAsString();
                T result = enumFromString(primitive.getAsString(),clazz.getEnumConstants(),null);
                if(result == null)
                    throw new JsonSyntaxException("Could not parse '" + s + "' as a " + enumName);
                return result;
            }
            throw new JsonSyntaxException("Expected " + entry + " to be a " + enumName + ", was " + GsonHelper.getType(e));
        }
        throw new JsonSyntaxException("Missing " + entry + ", expected to find a " + enumName);
    }

    public static String resourceSafeName(Enum<?> value) { return value.toString().toLowerCase(Locale.ENGLISH); }
    public static String prettyName(Enum<?> value) {
        StringBuilder result = new StringBuilder();
        String uglyString = value.toString();
        boolean capitalize = true;
        for(int i = 0; i < uglyString.length(); ++i)
        {
            char c = uglyString.charAt(i);
            if(c == '_')
            {
                result.append(' ');
                capitalize = true;
            }
            else
            {
                result.append(capitalize ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalize = false;
            }
        }
        return result.toString();
    }

}

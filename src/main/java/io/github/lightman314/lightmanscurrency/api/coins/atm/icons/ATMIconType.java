package io.github.lightman314.lightmanscurrency.api.coins.atm.icons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.helpers.data.CodecInteractionHelper;
import net.minecraft.IdentifierException;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ATMIconType {
    private final BiFunction<JsonObject, CodecInteractionHelper<JsonElement>,ATMIconData> deserializer;

    public ATMIconData parse(JsonObject data, CodecInteractionHelper<JsonElement> context) throws JsonSyntaxException, IdentifierException { return this.deserializer.apply(data,context); }

    private ATMIconType(BiFunction<JsonObject, CodecInteractionHelper<JsonElement>,ATMIconData> deserializer) { this.deserializer = deserializer; }

    public static ATMIconType create(Function<JsonObject,ATMIconData> deserializer) { return new ATMIconType((j, l) -> deserializer.apply(j)); }

    public static ATMIconType create(BiFunction<JsonObject, CodecInteractionHelper<JsonElement>,ATMIconData> deserializer) { return new ATMIconType(deserializer); }

}
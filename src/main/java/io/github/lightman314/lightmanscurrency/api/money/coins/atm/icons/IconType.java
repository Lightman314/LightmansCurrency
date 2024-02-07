package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Function;

public final class IconType {
    public final ResourceLocation type;
    private final Function<JsonObject,ATMIconData> deserializer;

    public boolean matches(String type) { return this.type.toString().equals(type); }

    public ATMIconData parse(JsonObject data) throws JsonSyntaxException, ResourceLocationException { return this.deserializer.apply(data); }

    private IconType(@Nonnull ResourceLocation type, @Nonnull Function<JsonObject,ATMIconData> deserializer) {
        this.type = type;
        this.deserializer = deserializer;
    }

    public static IconType create(ResourceLocation type, Function<JsonObject,ATMIconData> deserializer) { return new IconType(type, deserializer); }

}

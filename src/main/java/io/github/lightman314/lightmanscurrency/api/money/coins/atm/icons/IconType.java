package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class IconType {
    public final ResourceLocation type;
    private final BiFunction<JsonObject,HolderLookup.Provider,ATMIconData> deserializer;

    public boolean matches(String type) { return this.type.toString().equals(type); }

    public ATMIconData parse(JsonObject data,HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException { return this.deserializer.apply(data,lookup); }

    private IconType(ResourceLocation type, BiFunction<JsonObject,HolderLookup.Provider,ATMIconData> deserializer) {
        this.type = type;
        this.deserializer = deserializer;
    }

    public static IconType create(ResourceLocation type, Function<JsonObject,ATMIconData> deserializer) { return new IconType(type, (j,l) -> deserializer.apply(j)); }
    
    public static IconType create(ResourceLocation type, BiFunction<JsonObject,HolderLookup.Provider,ATMIconData> deserializer) { return new IconType(type, deserializer); }

}

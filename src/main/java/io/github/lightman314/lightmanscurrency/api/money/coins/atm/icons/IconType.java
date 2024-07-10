package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class IconType {
    public final ResourceLocation type;
    private final BiFunction<JsonObject,HolderLookup.Provider,ATMIconData> deserializer;

    public boolean matches(String type) { return this.type.toString().equals(type); }

    @Nonnull
    public ATMIconData parse(@Nonnull JsonObject data,@Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException { return this.deserializer.apply(data,lookup); }

    private IconType(@Nonnull ResourceLocation type, @Nonnull BiFunction<JsonObject,HolderLookup.Provider,ATMIconData> deserializer) {
        this.type = type;
        this.deserializer = deserializer;
    }

    @Nonnull
    public static IconType create(@Nonnull ResourceLocation type, @Nonnull Function<JsonObject,ATMIconData> deserializer) { return new IconType(type, (j,l) -> deserializer.apply(j)); }
    @Nonnull
    public static IconType create(@Nonnull ResourceLocation type, @Nonnull BiFunction<JsonObject,HolderLookup.Provider,ATMIconData> deserializer) { return new IconType(type, deserializer); }

}

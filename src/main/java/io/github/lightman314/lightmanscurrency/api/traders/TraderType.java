package io.github.lightman314.lightmanscurrency.api.traders;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public final class TraderType<T extends TraderData> {

    public final ResourceLocation type;
    private final Supplier<T> generator;

    public TraderType(@Nonnull ResourceLocation type, @Nonnull Supplier<T> generator)
    {
        this.type = type;
        this.generator = generator;
    }

    public T create() { return this.generator.get(); }

    public T load(boolean isClient, @Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
    {
        T trader = this.generator.get();
        trader.load(tag, lookup);
        if(isClient)
            trader.flagAsClient();
        return trader;
    }

    public T loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup)
    {
        T trader = this.generator.get();
        trader.loadFromJson(json, lookup);
        return trader;
    }

    @Override
    public String toString() { return this.type.toString(); }
}

package io.github.lightman314.lightmanscurrency.api.traders;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public final class TraderType<T extends TraderData> {

    public final ResourceLocation type;
    private final NonNullSupplier<T> generator;

    public TraderType(@Nonnull ResourceLocation type, @Nonnull NonNullSupplier<T> generator)
    {
        this.type = type;
        this.generator = generator;
    }

    public T create() { return this.generator.get(); }

    public T load(boolean isClient, @Nonnull CompoundTag tag)
    {
        T trader = this.generator.get();
        trader.load(tag);
        if(isClient)
            trader.flagAsClient();
        return trader;
    }

    public T loadFromJson(@Nonnull JsonObject json)
    {
        T trader = this.generator.get();
        trader.loadFromJson(json);
        return trader;
    }

    @Override
    public String toString() { return this.type.toString(); }
}

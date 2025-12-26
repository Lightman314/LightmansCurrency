package io.github.lightman314.lightmanscurrency.api.traders.rules;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

public final class TradeRuleType<T extends TradeRule> {

    public final ResourceLocation type;
    private final Supplier<T> generator;

    public TradeRuleType(@Nonnull ResourceLocation type, @Nonnull Supplier<T> generator)
    {
        this.type = type;
        this.generator = generator;
    }

    @Nonnull
    public T createNew() { return this.generator.get(); }

    public T load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
    {
        try {
            T rule = this.createNew();
            rule.load(tag, lookup);
            return rule;
        } catch (Throwable t) { LightmansCurrency.LogError("Error loading Trade Rule!", t); return null; }
    }

    public T loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException
    {
        T rule = this.createNew();
        rule.loadFromJson(json, lookup);
        rule.setActive(true);
        return rule;
    }

    @Override
    public int hashCode() {return this.type.hashCode(); }

    @Override
    public String toString() { return this.type.toString(); }

}

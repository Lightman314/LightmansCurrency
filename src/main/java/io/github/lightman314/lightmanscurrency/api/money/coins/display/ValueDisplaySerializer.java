package io.github.lightman314.lightmanscurrency.api.money.coins.display;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class ValueDisplaySerializer {
    @Nonnull
    public abstract ResourceLocation getType();
    public abstract void resetBuilder();
    public void parseAdditionalFromCoin(@Nonnull CoinEntry coin, @Nonnull JsonObject coinEntry) {}
    public abstract void parseAdditional(@Nonnull JsonObject chainJson);
    public void writeAdditionalToCoin(@Nonnull ValueDisplayData data, @Nonnull CoinEntry coin, @Nonnull JsonObject coinEntry) {}
    public abstract void writeAdditional(@Nonnull ValueDisplayData data, @Nonnull JsonObject chainJson);
    @Nonnull
    public abstract ValueDisplayData build() throws JsonSyntaxException;
}

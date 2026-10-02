package io.github.lightman314.lightmanscurrency.api.coins.display;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.IdentifierException;
import net.minecraft.core.Registry;

public abstract class ValueDisplaySerializer extends AbstractType<ValueDisplaySerializer> {

    @Override
    protected final Registry<ValueDisplaySerializer> getRegistry() { return LCRegistries.Coins.VALUE_DISPLAY_SERIALIZER; }

    @Override
    protected String getName() { return "ValueDisplaySerializer"; }

    public abstract void resetBuilder();
    public void parseAdditionalFromCoin(CoinEntry coin, JsonObject coinEntry) throws JsonSyntaxException, IdentifierException {}
    public abstract void parseAdditional(JsonObject chainJson) throws JsonSyntaxException, IdentifierException;
    public void writeAdditionalToCoin(ValueDisplayData data, CoinEntry coin, JsonObject coinEntry) {}
    public abstract void writeAdditional(ValueDisplayData data, JsonObject chainJson);
    public abstract ValueDisplayData build() throws JsonSyntaxException;
}

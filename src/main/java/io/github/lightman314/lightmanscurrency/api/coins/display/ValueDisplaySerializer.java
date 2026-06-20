package io.github.lightman314.lightmanscurrency.api.coins.display;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import net.minecraft.IdentifierException;

public abstract class ValueDisplaySerializer {

    public abstract void resetBuilder();
    public void parseAdditionalFromCoin(CoinEntry coin, JsonObject coinEntry) throws JsonSyntaxException, IdentifierException {}
    public abstract void parseAdditional(JsonObject chainJson) throws JsonSyntaxException, IdentifierException;
    public void writeAdditionalToCoin(ValueDisplayData data, CoinEntry coin, JsonObject coinEntry) {}
    public abstract void writeAdditional(ValueDisplayData data, JsonObject chainJson);
    public abstract ValueDisplayData build() throws JsonSyntaxException;
}

package io.github.lightman314.lightmanscurrency.api.coins.display.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class NullDisplay extends ValueDisplayData {

    public static final NullDisplay INSTANCE = new NullDisplay();
    public static final ValueDisplaySerializer SERIALIZER = new Serializer();

    private NullDisplay() {}
    @Override
    public ValueDisplaySerializer getSerializer() { return SERIALIZER; }
    @Override
    public Component formatValue(CoinValue value, Component emptyText) { return emptyText; }
    @Override
    public void formatCoinTooltip(ItemStack stack, List<Component> tooltip) { }

    protected static class Serializer extends ValueDisplaySerializer
    {
        @Override
        public void resetBuilder() { }
        @Override
        public void parseAdditional(JsonObject chainJson) throws JsonSyntaxException, IdentifierException { }
        @Override
        public void writeAdditional(ValueDisplayData data, JsonObject chainJson) throws JsonSyntaxException, IdentifierException { }
        @Override
        public NullDisplay build() { return INSTANCE; }
    }

}

package io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Null extends ValueDisplayData {

    public static final ResourceLocation TYPE = LightmansCurrency.id("null");
    public static final Null INSTANCE = new Null();
    public static final ValueDisplaySerializer SERIALIZER = new Serializer();

    private Null() {}
    @Override
    public ValueDisplaySerializer getSerializer() { return SERIALIZER; }
    @Override
    public Component formatValue(CoinValue value, Component emptyText) { return emptyText; }
    @Override
    public void formatCoinTooltip(ItemStack stack, List<Component> tooltip) { }

    protected static class Serializer extends ValueDisplaySerializer
    {
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public void resetBuilder() { }
        @Override
        public void parseAdditional(JsonObject chainJson) throws JsonSyntaxException, ResourceLocationException { }
        @Override
        public void writeAdditional(ValueDisplayData data, JsonObject chainJson) throws JsonSyntaxException, ResourceLocationException { }
        @Override
        public Null build() { return INSTANCE; }
    }

}

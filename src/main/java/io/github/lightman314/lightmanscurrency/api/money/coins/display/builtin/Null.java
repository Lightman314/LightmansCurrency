package io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class Null extends ValueDisplayData {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("null");
    public static final Null INSTANCE = new Null();
    public static final ValueDisplaySerializer SERIALIZER = new Serializer();

    private Null() {}
    @Nonnull
    @Override
    public ValueDisplaySerializer getSerializer() { return SERIALIZER; }
    @Nonnull
    @Override
    public MutableComponent formatValue(@Nonnull CoinValue value, @Nonnull MutableComponent emptyText) { return emptyText; }
    @Override
    public void formatCoinTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip) { }

    protected static class Serializer extends ValueDisplaySerializer
    {
        @Nonnull
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public void resetBuilder() { }
        @Override
        public void parseAdditional(@Nonnull JsonObject chainJson) throws JsonSyntaxException, ResourceLocationException { }
        @Override
        public void writeAdditional(@Nonnull ValueDisplayData data, @Nonnull JsonObject chainJson) throws JsonSyntaxException, ResourceLocationException { }
        @Nonnull
        @Override
        public Null build() { return INSTANCE; }
    }

}

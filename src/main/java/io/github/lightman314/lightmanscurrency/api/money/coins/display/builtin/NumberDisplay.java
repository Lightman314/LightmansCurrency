package io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class NumberDisplay extends ValueDisplayData {

    public static final ResourceLocation TYPE = new ResourceLocation(MoneyAPI.MODID, "number");
    public static final ValueDisplaySerializer SERIALIZER = new Serializer();

    private final Component format;
    public final Component getFormat() { return this.format.copy(); }
    private final Component wordyFormat;
    public final Component getWordyFormat() { return this.wordyFormat != null ? this.wordyFormat.copy() : this.getFormat(); }
    private final Item baseItem;
    private CoinEntry baseEntry = null;

    @Nullable
    private CoinEntry getBaseEntry()
    {
        if(this.baseEntry == null)
        {
            ChainData parent = this.getParent();
            if(parent != null)
                this.baseEntry = parent.findEntry(this.baseItem);
        }
        return this.baseEntry;
    }

    public NumberDisplay(@Nonnull Component format, @Nonnull Item baseItem) {
        this.format = format;
        this.wordyFormat = format;
        this.baseItem = baseItem;
    }
    public NumberDisplay(@Nonnull Component format, @Nullable Component wordyFormat, @Nonnull Item baseItem)
    {
        this.format = format;
        this.wordyFormat = wordyFormat;
        this.baseItem = baseItem;
    }

    @Nonnull
    @Override
    public ValueDisplaySerializer getSerializer() { return SERIALIZER; }

    public double getDisplayValue(long coreValue) {
        CoinEntry baseUnit = this.getBaseEntry();
        //Null & divide by zero check
        if(baseUnit == null || baseUnit.getCoreValue() <= 0)
            return 0d;
        return (double)coreValue/(double)baseUnit.getCoreValue();
    }
    private double getDisplayValue(@Nonnull Item item)
    {
        ChainData parent = this.getParent();
        if(parent == null)
            return 0d;
        return getDisplayValue(parent.getCoreValue(item));
    }

    private String formatDisplay(double value) { return this.format.getString().replace("{value}", this.formatDisplayNumber(value)); }
    private String formatWordyDisplay(double value) { return this.getWordyFormat().getString().replace("{value}", this.formatDisplayNumber(value)); }

    private String formatDisplayNumber(double value)
    {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(this.getMaxDecimal());
        return df.format(value);
    }

    private int getMaxDecimal()
    {
        double minFraction = this.getDisplayValue(1) % 1d;
        if(minFraction > 0d)
        {
            //-2 to ignore the 0.
            return Double.toString(minFraction).length() - 2;
        }
        else
            return 0;
    }

    @Nonnull
    @Override
    public MutableComponent formatValue(@Nonnull CoinValue value, @Nonnull MutableComponent emptyText) {
        return EasyText.literal(this.formatDisplay(this.getDisplayValue(value.getCoreValue())));
    }

    @Override
    public void formatCoinTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip) {
        double value = this.getDisplayValue(stack.getItem());
        tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.value", this.formatWordyDisplay(value)).withStyle(ChatFormatting.YELLOW));
        if (stack.getCount() > 1)
            tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.value.stack", this.formatWordyDisplay(value * stack.getCount())).withStyle(ChatFormatting.YELLOW));
    }

    @Nonnull
    @Override
    public MoneyValue parseDisplayInput(double displayInput) {
        CoinEntry baseUnit = this.getBaseEntry();
        if(baseUnit == null)
            return MoneyValue.empty();
        long baseCoinValue = baseUnit.getCoreValue();
        double totalValue = displayInput * baseCoinValue;
        long value = (long)totalValue;
        if(totalValue % 1d >= 0.5d)
            value++;
        return CoinValue.fromNumber(this.getChain(), value);
    }

    protected static class Serializer extends ValueDisplaySerializer
    {

        private Component format = null;
        private Component wordyFormat = null;
        private Item baseUnit = null;

        @Nonnull
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public void resetBuilder() { this.format = null; this.wordyFormat = null; this.baseUnit = null; }
        @Override
        public void parseAdditional(@Nonnull JsonObject chainJson) {
            this.format = Component.Serializer.fromJson(chainJson.get("displayFormat"));
            if(chainJson.has("displayFormatWordy"))
                this.wordyFormat = Component.Serializer.fromJson(chainJson.get("displayFormatWordy"));
        }

        @Override
        public void parseAdditionalFromCoin(@Nonnull CoinEntry coin, @Nonnull JsonObject coinEntry) {
            if(GsonHelper.getAsBoolean(coinEntry, "baseUnit", false))
            {
                if(this.baseUnit != null)
                    throw new JsonSyntaxException("Cannot have two baseUnit entries!");
                this.baseUnit = coin.getCoin();
            }
        }

        @Override
        public void writeAdditional(@Nonnull ValueDisplayData data, @Nonnull JsonObject chainJson) {
            if(data instanceof NumberDisplay display)
            {
                chainJson.add("displayFormat", Component.Serializer.toJsonTree(display.format));
                if(display.wordyFormat != null)
                    chainJson.add("displayFormatWordy", Component.Serializer.toJsonTree(display.wordyFormat));
            }
        }

        @Override
        public void writeAdditionalToCoin(@Nonnull ValueDisplayData data, @Nonnull CoinEntry coin, @Nonnull JsonObject coinEntry) {
            if(data instanceof NumberDisplay display && coin.matches(display.baseItem))
                coinEntry.addProperty("baseUnit", true);
        }

        @Nonnull
        @Override
        public NumberDisplay build() throws JsonSyntaxException {
            if(this.format == null)
                throw new JsonSyntaxException("displayFormat entry is missing or cannot be parsed!");
            if(this.baseUnit == null)
                throw new JsonSyntaxException("No coin entry has the 'baseUnit: true' flag!");
            return new NumberDisplay(this.format, this.wordyFormat, this.baseUnit);
        }
    }

}

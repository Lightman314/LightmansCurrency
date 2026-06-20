package io.github.lightman314.lightmanscurrency.api.coins.display.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class NumberDisplay extends ValueDisplayData {

    public static final ValueDisplaySerializer SERIALIZER = new Serializer();

    private final Pair<String,Boolean> format;
    private final Pair<String,Boolean> wordyFormat;
    private Pair<String,Boolean> getWordyFormat() { return this.wordyFormat != null ? this.wordyFormat : this.format; }
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

    public NumberDisplay(TextEntry format, Item baseItem) { this(Pair.of(format.getKey(),true),baseItem); }
    public NumberDisplay(String literalFormat, Item baseItem) { this(Pair.of(literalFormat,false),baseItem); }
    public NumberDisplay(Pair<String,Boolean> format, Item baseItem) {

        this.format = format;
        this.wordyFormat = format;
        this.baseItem = baseItem;
    }
    public NumberDisplay(TextEntry format, @Nullable TextEntry wordyFormat, Item baseItem) { this(Pair.of(format.getKey(),true),Pair.of(wordyFormat.getKey(),true),baseItem);}
    public NumberDisplay(String literalFormat, @Nullable String literalWordyFormat, Item baseItem) { this(Pair.of(literalFormat,false),literalWordyFormat == null ? null : Pair.of(literalWordyFormat,false),baseItem); }
    public NumberDisplay(Pair<String,Boolean> format, @Nullable Pair<String,Boolean> wordyFormat, Item baseItem)
    {
        this.format = format;
        this.wordyFormat = wordyFormat;
        this.baseItem = baseItem;
    }

    @Override
    public ValueDisplaySerializer getSerializer() { return SERIALIZER; }

    public double getDisplayValue(long coreValue) {
        CoinEntry baseUnit = this.getBaseEntry();
        //Null & divide by zero check
        if(baseUnit == null || baseUnit.getInternalValue() <= 0)
            return 0d;
        return (double)coreValue/(double)baseUnit.getInternalValue();
    }
    private double getDisplayValue(Item item)
    {
        ChainData parent = this.getParent();
        if(parent == null)
            return 0d;
        return getDisplayValue(parent.getInternalValue(item));
    }

    private Component formatDisplay(double value) { return this.format(this.format, LCText.Coins.TOOLTIP_COIN_DISPLAY_NUMBER,this.formatDisplayNumber(value)); }
    private Component formatWordyDisplay(double value) {
        TextEntry format = LCText.Coins.TOOLTIP_COIN_DISPLAY_NUMBER;
        if(this.wordyFormat != null)
            format = LCText.Coins.TOOLTIP_COIN_DISPLAY_NUMBER_WORDY;
        return this.format(this.getWordyFormat(),format,this.formatDisplayNumber(value));
    }

    protected Component getIcon() { return getIcon(this.getChain()); }

    private Component format(Pair<String,Boolean> format, TextEntry iconFormat, String value)
    {
        if(format.getSecond())
            return Component.translatable(format.getFirst(),iconFormat.get(value,this.getIcon()));
        else
            return Component.literal(String.format(format.getFirst().replace("{value}",value)));
    }


    private String formatDisplayNumber(double value)
    {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(this.getMaxDecimal());
        return df.format(value);
    }

    public int getMaxDecimal()
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


    @Override
    public Component formatValue(CoinValue value, Component emptyText) {
        return this.formatDisplay(this.getDisplayValue(value.getInternalValue()));
    }

    @Override
    public void formatCoinTooltip(ItemStack stack, List<Component> tooltip) {
        double value = this.getDisplayValue(stack.getItem());
        tooltip.add(LCText.Coins.TOOLTIP_COIN_WORTH_VALUE.get(this.formatWordyDisplay(value)).withStyle(ChatFormatting.YELLOW));
        if (stack.getCount() > 1)
            tooltip.add(LCText.Coins.TOOLTIP_COIN_WORTH_VALUE_STACK.get(this.formatWordyDisplay(value * stack.getCount())).withStyle(ChatFormatting.YELLOW));
    }


    @Override
    public MoneyValue parseDisplayInput(double displayInput) {
        CoinEntry baseUnit = this.getBaseEntry();
        if(baseUnit == null)
            return MoneyValue.empty();
        long baseCoinValue = baseUnit.getInternalValue();
        double totalValue = displayInput * baseCoinValue;
        long value = (long)totalValue;
        if(totalValue % 1d >= 0.5d)
            value++;
        return CoinValue.fromNumber(this.getChain(), value);
    }


    public Pair<String,String> getSplitFormat() { return this.splitFormat(this.format,LCText.Coins.TOOLTIP_COIN_DISPLAY_NUMBER); }

    public Pair<String,String> getSplitWordyFormat() {
        TextEntry format = LCText.Coins.TOOLTIP_COIN_DISPLAY_NUMBER;
        if(this.wordyFormat != null)
            format = LCText.Coins.TOOLTIP_COIN_DISPLAY_NUMBER_WORDY;
        return this.splitFormat(this.getWordyFormat(),format);
    }

    private Pair<String,String> splitFormat(Pair<String,Boolean> format, TextEntry iconFormat)
    {
        //Have to replace the {value} with a non-illegal character in order to split the string
        String formatString = this.format(format,iconFormat,"`").getString();
        String[] splitFormat = formatString.split("`",2);
        if(splitFormat.length < 2)
        {
            //Determine which is the prefix, and which is the postfix
            if(formatString.startsWith("`"))
                return Pair.of("",splitFormat[0]);
            else
                return Pair.of(splitFormat[0],"");
        }
        return Pair.of(splitFormat[0],splitFormat[1]);
    }

    protected static class Serializer extends ValueDisplaySerializer
    {

        private Pair<String,Boolean> format = null;
        private Pair<String,Boolean> wordyFormat = null;
        private Item baseUnit = null;
        private Item firstCoin = null;

        @Override
        public void resetBuilder() { this.format = this.wordyFormat = null; this.baseUnit = this.firstCoin = null; }
        @Override
        public void parseAdditional(JsonObject chainJson) throws JsonSyntaxException, IdentifierException {
            this.format = parseFormat(chainJson,"displayFormat");
            if(chainJson.has("displayFormatWordy"))
                this.wordyFormat = parseFormat(chainJson,"displayFormatWordy");
        }

        @Override
        public void parseAdditionalFromCoin(CoinEntry coin, JsonObject coinEntry) throws JsonSyntaxException, IdentifierException {
            if(GsonHelper.getAsBoolean(coinEntry, "baseUnit", false))
            {
                if(this.baseUnit != null)
                    LightmansCurrency.LogWarning("Multiple coins in this chain have a 'baseUnit' flag! Ignoring duplicate entries.");
                this.baseUnit = coin.getCoin();
            }
            else if(this.firstCoin == null)
                this.firstCoin = coin.getCoin();
        }

        @Override
        public void writeAdditional(ValueDisplayData data, JsonObject chainJson) {
            if(data instanceof NumberDisplay display)
            {
                saveFormat(chainJson,"displayFormat", display.format);
                if(display.wordyFormat != null)
                    saveFormat(chainJson,"displayFormatWordy", display.wordyFormat);
            }
        }

        @Override
        public void writeAdditionalToCoin(ValueDisplayData data, CoinEntry coin, JsonObject coinEntry) {
            if(data instanceof NumberDisplay display && coin.matches(display.baseItem))
                coinEntry.addProperty("baseUnit", true);
        }


        @Override
        public NumberDisplay build() throws JsonSyntaxException {
            if(this.format == null)
                throw new JsonSyntaxException("displayFormat entry is missing or cannot be parsed!");
            if(this.baseUnit == null && this.firstCoin == null)
                throw new JsonSyntaxException("No coins could be found to be designated as the base unit!");
            return new NumberDisplay(this.format, this.wordyFormat, this.baseUnit == null ? this.firstCoin : this.baseUnit);
        }


        private static Pair<String,Boolean> parseFormat(JsonObject json, String key) throws JsonSyntaxException
        {
            JsonElement element = json.get(key);
            if(element == null)
                throw new JsonSyntaxException("Missing " + key);
            else
            {
                if(element.isJsonPrimitive())
                    return Pair.of(GsonHelper.convertToString(element,key),false);
                else
                {
                    JsonObject object = GsonHelper.convertToJsonObject(element,key);
                    if(object.has("translate"))
                        return Pair.of(GsonHelper.getAsString(object,"translate"),true);
                    return Pair.of(GsonHelper.getAsString(object,"text"),false);
                }
            }
        }

        private static void saveFormat(JsonObject json, String key, Pair<String,Boolean> format)
        {
            if(format.getSecond())
            {
                JsonObject o = new JsonObject();
                o.addProperty("translate",format.getFirst());
                json.add(key,o);
            }
            else
                json.addProperty(key,format.getFirst());
        }

    }

}
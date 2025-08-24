package io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
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

    public static final ResourceLocation TYPE = VersionUtil.lcResource("number");
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

    public NumberDisplay(@Nonnull TextEntry format, @Nonnull Item baseItem) { this(Pair.of(format.getKey(),true),baseItem); }
    public NumberDisplay(@Nonnull String literalFormat, @Nonnull Item baseItem) { this(Pair.of(literalFormat,false),baseItem); }
    public NumberDisplay(@Nonnull Pair<String,Boolean> format, @Nonnull Item baseItem) {

        this.format = format;
        this.wordyFormat = format;
        this.baseItem = baseItem;
    }
    public NumberDisplay(@Nonnull TextEntry format, @Nullable TextEntry wordyFormat, @Nonnull Item baseItem) { this(Pair.of(format.getKey(),true),Pair.of(wordyFormat.getKey(),true),baseItem);}
    public NumberDisplay(@Nonnull String literalFormat, @Nullable String literalWordyFormat, @Nonnull Item baseItem) { this(Pair.of(literalFormat,false),literalWordyFormat == null ? null : Pair.of(literalWordyFormat,false),baseItem); }
    public NumberDisplay(@Nonnull Pair<String,Boolean> format, @Nullable Pair<String,Boolean> wordyFormat, @Nonnull Item baseItem)
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

    private MutableComponent formatDisplay(double value) { return this.format(this.format,LCText.TOOLTIP_COIN_DISPLAY_NUMBER,this.formatDisplayNumber(value)); }
    private MutableComponent formatWordyDisplay(double value) {
        TextEntry format = LCText.TOOLTIP_COIN_DISPLAY_NUMBER;
        if(this.wordyFormat != null)
            format = LCText.TOOLTIP_COIN_DISPLAY_NUMBER_WORDY;
        return this.format(this.getWordyFormat(),format,this.formatDisplayNumber(value));
    }
    protected Component getIcon() { return getIcon(this.getChain()); }

    private MutableComponent format(@Nonnull Pair<String,Boolean> format, @Nonnull TextEntry iconFormat, @Nonnull String value)
    {
        if(format.getSecond())
            return EasyText.translatable(format.getFirst(),iconFormat.get(value,this.getIcon()));
        else
            return EasyText.literal(String.format(format.getFirst().replace("{value}",value)));
    }

    @Nonnull
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

    @Nonnull
    @Override
    public MutableComponent formatValue(@Nonnull CoinValue value, @Nonnull MutableComponent emptyText) {
        return this.formatDisplay(this.getDisplayValue(value.getCoreValue()));
    }

    @Override
    public void formatCoinTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip) {
        double value = this.getDisplayValue(stack.getItem());
        tooltip.add(LCText.TOOLTIP_COIN_WORTH_VALUE.get(this.formatWordyDisplay(value)).withStyle(ChatFormatting.YELLOW));
        if (stack.getCount() > 1)
            tooltip.add(LCText.TOOLTIP_COIN_WORTH_VALUE_STACK.get(this.formatWordyDisplay(value * stack.getCount())).withStyle(ChatFormatting.YELLOW));
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

    @Nonnull
    public Pair<String,String> getSplitFormat() { return this.splitFormat(this.format,LCText.TOOLTIP_COIN_DISPLAY_NUMBER); }
    @Nonnull
    public Pair<String,String> getSplitWordyFormat() {
        TextEntry format = LCText.TOOLTIP_COIN_DISPLAY_NUMBER;
        if(this.wordyFormat != null)
            format = LCText.TOOLTIP_COIN_DISPLAY_NUMBER_WORDY;
        return this.splitFormat(this.getWordyFormat(),format);
    }
    @Nonnull
    private Pair<String,String> splitFormat(@Nonnull Pair<String,Boolean> format, @Nonnull TextEntry iconFormat)
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

        @Nonnull
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public void resetBuilder() { this.format = null; this.wordyFormat = null; this.baseUnit = this.firstCoin = null; }
        @Override
        public void parseAdditional(@Nonnull JsonObject chainJson) throws JsonSyntaxException, ResourceLocationException {
            this.format = parseFormat(chainJson,"displayFormat");
            if(chainJson.has("displayFormatWordy"))
                this.wordyFormat = parseFormat(chainJson,"displayFormatWordy");
        }

        @Override
        public void parseAdditionalFromCoin(@Nonnull CoinEntry coin, @Nonnull JsonObject coinEntry) throws JsonSyntaxException, ResourceLocationException {
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
        public void writeAdditional(@Nonnull ValueDisplayData data, @Nonnull JsonObject chainJson) {
            if(data instanceof NumberDisplay display)
            {
                saveFormat(chainJson,"displayFormat", display.format);
                if(display.wordyFormat != null)
                    saveFormat(chainJson,"displayFormatWordy", display.wordyFormat);
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
            if(this.baseUnit == null && this.firstCoin == null)
                throw new JsonSyntaxException("No coins could be found to be designated as the base unit!");
            return new NumberDisplay(this.format, this.wordyFormat, this.baseUnit == null ? this.firstCoin : this.baseUnit);
        }

        @Nonnull
        private static Pair<String,Boolean> parseFormat(@Nonnull JsonObject json, @Nonnull String key) throws JsonSyntaxException
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

        private static void saveFormat(@Nonnull JsonObject json, @Nonnull String key, @Nonnull Pair<String,Boolean> format)
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
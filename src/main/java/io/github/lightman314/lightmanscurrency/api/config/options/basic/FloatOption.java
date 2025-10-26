package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FloatOption extends ConfigOption<Float> {

    public static ConfigParser<Float> makeParser(float lowerLimit, float upperLimit) { return new Parser(lowerLimit, upperLimit); }

    public final float lowerLimit;
    public final float upperLimit;
    private final ConfigParser<Float> parser;

    protected FloatOption(Supplier<Float> defaultValue, float lowerLimit, float upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = makeParser(lowerLimit,upperLimit);
    }

    @Override
    public boolean allowedValue(Float newValue) { return newValue >= this.lowerLimit && newValue <= this.upperLimit; }

    @Override
    protected ConfigParser<Float> getParser() { return this.parser; }
    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList(
            "Range: " + this.lowerLimit + " -> " + this.upperLimit,
            "Default: " + this.getDefaultValue()
        );
    }

    @Override
    protected List<Component> bonusCommentTooltips() {
        return Lists.newArrayList(
                LCText.CONFIG_OPTION_RANGE.get(this.lowerLimit,this.upperLimit),
                LCText.CONFIG_OPTION_DEFAULT.get(this.getDefaultValue())
        );
    }

    public static FloatOption create(float defaultValue) { return new FloatOption(() -> defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE); }
    public static FloatOption create(float defaultValue, float lowerLimit) { return new FloatOption(() -> defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatOption create(float defaultValue, float lowerLimit, float upperLimit) { return new FloatOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static FloatOption create(Supplier<Float> defaultValue) { return new FloatOption(defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE); }
    public static FloatOption create(Supplier<Float> defaultValue, float lowerLimit) { return new FloatOption(defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatOption create(Supplier<Float> defaultValue, float lowerLimit, float upperLimit) { return new FloatOption(defaultValue, lowerLimit, upperLimit); }



    private static class Parser implements ConfigParser<Float>
    {
        private final float lowerLimit;
        private final float upperLimit;

        private Parser(float lowerLimit, float upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        
        @Override
        public Float tryParse(String cleanLine) throws ConfigParsingException {
            try { return MathUtil.clamp(Float.parseFloat(cleanLine), this.lowerLimit, this.upperLimit);
            } catch (NumberFormatException e) { throw new ConfigParsingException("Error parsing float!", e); }
        }
        
        @Override
        public String write(Float value) { return value.toString(); }
    }


}

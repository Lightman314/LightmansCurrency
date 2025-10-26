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
public class DoubleOption extends ConfigOption<Double> {

    public static ConfigParser<Double> makeParser(double lowerLimit, double upperLimit) { return new Parser(lowerLimit, upperLimit); }

    public final double lowerLimit;
    public final double upperLimit;
    private final ConfigParser<Double> parser;

    protected DoubleOption(Supplier<Double> defaultValue, double lowerLimit, double upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = makeParser(lowerLimit,upperLimit);
    }

    @Override
    public boolean allowedValue(Double newValue) { return newValue >= this.lowerLimit && newValue <= this.upperLimit; }

    @Override
    protected ConfigParser<Double> getParser() { return this.parser; }

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

    public static DoubleOption create(double defaultValue) { return new DoubleOption(() -> defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE); }
    public static DoubleOption create(double defaultValue, double lowerLimit) { return new DoubleOption(() -> defaultValue, lowerLimit, Double.MAX_VALUE); }
    public static DoubleOption create(double defaultValue, double lowerLimit, double upperLimit) { return new DoubleOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static DoubleOption create(Supplier<Double> defaultValue) { return new DoubleOption(defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE); }
    public static DoubleOption create(Supplier<Double> defaultValue, double lowerLimit) { return new DoubleOption(defaultValue, lowerLimit, Double.MAX_VALUE); }
    public static DoubleOption create(Supplier<Double> defaultValue, double lowerLimit, double upperLimit) { return new DoubleOption(defaultValue, lowerLimit, upperLimit); }

    private static class Parser implements ConfigParser<Double>
    {
        private final double lowerLimit;
        private final double upperLimit;

        private Parser(double lowerLimit, double upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        
        @Override
        public Double tryParse(String cleanLine) throws ConfigParsingException {
            try { return MathUtil.clamp(Double.parseDouble(cleanLine), this.lowerLimit, this.upperLimit);
            } catch (NumberFormatException e) { throw new ConfigParsingException("Error parsing double!", e); }
        }
        
        @Override
        public String write(Double value) { return value.toString(); }
    }


}

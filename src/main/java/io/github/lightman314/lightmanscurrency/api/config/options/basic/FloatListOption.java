package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FloatListOption extends ListOption<Float> {

    public final float lowerLimit;
    public final float upperLimit;
    private final ConfigParser<Float> parser;
    protected FloatListOption(Supplier<List<Float>> defaultValue, float lowerLimit, float upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = FloatOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    public boolean allowedListValue(Float newValue) { return newValue >= this.lowerLimit && newValue <= this.upperLimit; }

    @Override
    protected ConfigParser<Float> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_RANGE.get(this.lowerLimit,this.upperLimit); }

    public static FloatListOption create(List<Float> defaultValue) { return new FloatListOption(() -> defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE); }
    public static FloatListOption create(List<Float> defaultValue, float lowerLimit) { return new FloatListOption(() -> defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatListOption create(List<Float> defaultValue, float lowerLimit, float upperLimit) { return new FloatListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static FloatListOption create(Supplier<List<Float>> defaultValue) { return new FloatListOption(defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE); }
    public static FloatListOption create(Supplier<List<Float>> defaultValue, float lowerLimit) { return new FloatListOption(defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatListOption create(Supplier<List<Float>> defaultValue, float lowerLimit, float upperLimit) { return new FloatListOption(defaultValue, lowerLimit, upperLimit); }

}

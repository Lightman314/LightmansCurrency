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
public class IntListOption extends ListOption<Integer> {

    public final int lowerLimit;
    public final int upperLimit;
    private final ConfigParser<Integer> parser;
    protected IntListOption(Supplier<List<Integer>> defaultValue, int lowerLimit, int upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = IntOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    public boolean allowedListValue(Integer newValue) { return newValue >= this.lowerLimit && newValue <= this.upperLimit; }

    @Override
    protected ConfigParser<Integer> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_RANGE.get(this.lowerLimit,this.upperLimit); }

    public static IntListOption create(List<Integer> defaultValue) { return new IntListOption(() -> defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntListOption create(List<Integer> defaultValue, int lowerLimit) { return new IntListOption(() -> defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntListOption create(List<Integer> defaultValue, int lowerLimit, int upperLimit) { return new IntListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static IntListOption create(Supplier<List<Integer>> defaultValue) { return new IntListOption(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntListOption create(Supplier<List<Integer>> defaultValue, int lowerLimit) { return new IntListOption(defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntListOption create(Supplier<List<Integer>> defaultValue, int lowerLimit, int upperLimit) { return new IntListOption(defaultValue, lowerLimit, upperLimit); }

}
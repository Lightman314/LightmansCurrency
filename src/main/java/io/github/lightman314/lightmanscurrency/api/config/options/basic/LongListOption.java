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
public class LongListOption extends ListOption<Long> {

    public final long lowerLimit;
    public final long upperLimit;
    private final ConfigParser<Long> parser;
    protected LongListOption(Supplier<List<Long>> defaultValue, long lowerLimit, long upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = LongOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    public boolean allowedListValue(Long newValue) { return newValue >= this.lowerLimit && newValue <= this.upperLimit; }

    @Override
    protected ConfigParser<Long> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_RANGE.get(this.lowerLimit,this.upperLimit); }

    public static LongListOption create(List<Long> defaultValue) { return new LongListOption(() -> defaultValue, Long.MIN_VALUE, Long.MAX_VALUE); }
    public static LongListOption create(List<Long> defaultValue, long lowerLimit) { return new LongListOption(() -> defaultValue, lowerLimit, Long.MAX_VALUE); }
    public static LongListOption create(List<Long> defaultValue, long lowerLimit, long upperLimit) { return new LongListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static LongListOption create(Supplier<List<Long>> defaultValue) { return new LongListOption(defaultValue, Long.MIN_VALUE, Long.MAX_VALUE); }
    public static LongListOption create(Supplier<List<Long>> defaultValue, long lowerLimit) { return new LongListOption(defaultValue, lowerLimit, Long.MAX_VALUE); }
    public static LongListOption create(Supplier<List<Long>> defaultValue, long lowerLimit, long upperLimit) { return new LongListOption(defaultValue, lowerLimit, upperLimit); }

}
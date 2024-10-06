package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.IntOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ScreenPositionOption extends ConfigOption<ScreenPosition> {

    public static final ConfigParser<ScreenPosition> PARSER = new Parser();
    public static final ConfigParser<Integer> INT_PARSER = IntOption.makeParser(-10000,10000);

    protected ScreenPositionOption(@Nonnull Supplier<ScreenPosition> defaultValue) { super(defaultValue); }

    @Nonnull
    @Override
    protected ConfigParser<ScreenPosition> getParser() { return PARSER; }

    public static ScreenPositionOption create(int x, int y) { return create(ScreenPosition.of(x,y)); }
    public static ScreenPositionOption create(@Nonnull ScreenPosition defaultValue) { return new ScreenPositionOption(() -> defaultValue); }
    public static ScreenPositionOption create(@Nonnull Supplier<ScreenPosition> defaultValue) { return new ScreenPositionOption(defaultValue); }

    private static class Parser implements ConfigParser<ScreenPosition>
    {
        @Nonnull
        @Override
        public ScreenPosition tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            String[] split = cleanLine.split(",",2);
            if(split.length != 2)
                throw new ConfigParsingException("Extra or missing comma(s)!");
            return ScreenPosition.of(INT_PARSER.tryParse(split[0]),INT_PARSER.tryParse(split[1]));
        }
        @Nonnull
        @Override
        public String write(@Nonnull ScreenPosition value) { return value.x + "," + value.y; }
    }

}

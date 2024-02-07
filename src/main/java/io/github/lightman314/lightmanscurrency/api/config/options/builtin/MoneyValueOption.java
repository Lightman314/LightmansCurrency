package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class MoneyValueOption extends ConfigOption<MoneyValue> {

    public static ConfigParser<MoneyValue> createParser(@Nonnull Predicate<MoneyValue> allowed) { return new Parser(allowed); }

    private final ConfigParser<MoneyValue> parser;

    protected MoneyValueOption(@Nonnull NonNullSupplier<MoneyValue> defaultValue, @Nonnull Predicate<MoneyValue> allowed) { super(defaultValue); this.parser = createParser(allowed); }

    @Nonnull
    @Override
    protected ConfigParser<MoneyValue> getParser() { return this.parser; }

    public static MoneyValueOption create(@Nonnull NonNullSupplier<MoneyValue> defaultValue) { return create(defaultValue, v -> true); }
    public static MoneyValueOption createNonEmpty(@Nonnull NonNullSupplier<MoneyValue> defaultValue) { return create(defaultValue, v -> !v.isEmpty()); }
    public static MoneyValueOption create(@Nonnull NonNullSupplier<MoneyValue> defaultValue, @Nonnull Predicate<MoneyValue> allowed) { return new MoneyValueOption(defaultValue, allowed); }

    private static class Parser implements ConfigParser<MoneyValue>
    {
        private final Predicate<MoneyValue> allowed;
        private Parser(@Nonnull Predicate<MoneyValue> allowed) { this.allowed = allowed; }
        @Nonnull
        @Override
        public MoneyValue tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            try {
                MoneyValue result = MoneyValueParser.parse(new StringReader(StringOption.PARSER.tryParse(cleanLine)), true);
                if(!this.allowed.test(result))
                    throw new ConfigParsingException(cleanLine + " is not an allowed Money Value input!");
                return result;
            } catch (CommandSyntaxException e) { throw new ConfigParsingException(e); }
        }
        @Nonnull
        @Override
        public String write(@Nonnull MoneyValue value) { return StringOption.PARSER.write(MoneyValueParser.writeParsable(value)); }
    }

}

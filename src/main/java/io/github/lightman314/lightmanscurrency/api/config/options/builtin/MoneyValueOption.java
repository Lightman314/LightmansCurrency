package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyValueOption extends ConfigOption<MoneyValue> {

    public static final String bonusComment = "See the wiki for Money Value format: https://github.com/Lightman314/LightmansCurrency/wiki/Money-Value-Arguments";

    public static ConfigParser<MoneyValue> createParser(Predicate<MoneyValue> allowed) { return new Parser(allowed); }

    private final ConfigParser<MoneyValue> parser;

    public final Predicate<MoneyValue> allowed;
    protected MoneyValueOption(Supplier<MoneyValue> defaultValue, Predicate<MoneyValue> allowed) { super(defaultValue); this.parser = createParser(allowed); this.allowed = allowed; }

    @Override
    public boolean allowedValue(MoneyValue newValue) { return this.allowed.test(newValue); }

    @Override
    protected ConfigParser<MoneyValue> getParser() { return this.parser; }

    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList(
                bonusComment,
                "Default: " + this.parser.write(this.getDefaultValue())
        );
    }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_DEFAULT.get(this.getDefaultValue().getText(LCText.GUI_MONEY_STORAGE_EMPTY.get())); }

    public static MoneyValueOption create(Supplier<MoneyValue> defaultValue) { return create(defaultValue, v -> true); }
    public static MoneyValueOption createNonEmpty(Supplier<MoneyValue> defaultValue) { return create(defaultValue, v -> !v.isEmpty()); }
    public static MoneyValueOption create(Supplier<MoneyValue> defaultValue, Predicate<MoneyValue> allowed) { return new MoneyValueOption(defaultValue, allowed); }

    private static class Parser implements ConfigParser<MoneyValue>
    {
        private final Predicate<MoneyValue> allowed;
        private Parser(Predicate<MoneyValue> allowed) { this.allowed = allowed; }

        @Override
        public MoneyValue tryParse(String cleanLine) throws ConfigParsingException {
            try {
                MoneyValue result = MoneyValueParser.parse(new StringReader(StringOption.PARSER.tryParse(cleanLine)), true);
                if(!this.allowed.test(result))
                    throw new ConfigParsingException(cleanLine + " is not an allowed Money Value input!");
                return result;
            } catch (CommandSyntaxException e) { throw new ConfigParsingException(e); }
        }

        @Override
        public String write(MoneyValue value) { return StringOption.PARSER.write(MoneyValueParser.writeParsable(value)); }
    }

}
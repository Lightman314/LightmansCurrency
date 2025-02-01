package io.github.lightman314.lightmanscurrency.common.config;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.ItemOverride;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemOverrideListOption extends ListOption<ItemOverride> {

    public static final ConfigParser<ItemOverride> PARSER = new Parser();

    private ItemOverrideListOption(@Nonnull Supplier<List<ItemOverride>> defaultValue) { super(defaultValue); }

    public static ItemOverrideListOption of() { return of(ArrayList::new); }
    public static ItemOverrideListOption of(@Nonnull List<ItemOverride> defaultValue) { return of(() -> defaultValue); }
    public static ItemOverrideListOption of(@Nonnull Supplier<List<ItemOverride>> defaultValue) { return new ItemOverrideListOption(defaultValue); }

    @Override
    protected ConfigParser<ItemOverride> getPartialParser() { return PARSER; }

    @Nullable
    @Override
    protected String bonusComment() { return MoneyValueOption.bonusComment; }

    private static class Parser implements ConfigParser<ItemOverride>
    {
        @Nonnull
        @Override
        public ItemOverride tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            String string = StringOption.PARSER.tryParse(cleanLine);
            String[] split = string.split("\\|");
            if(split.length <= 1)
                throw new ConfigParsingException("Missing '|' splitter");
            if(split.length > 2)
                throw new ConfigParsingException("More than one '|' splitter");
            try {
                MoneyValue baseCost = MoneyValueParser.parse(new StringReader(split[0]),false);
                String[] split2 = split[1].split(",");
                return new ItemOverride(baseCost, ImmutableList.copyOf(split2));
            } catch (CommandSyntaxException e) { throw new ConfigParsingException(e.getMessage()); }
        }
        @Nonnull
        @Override
        public String write(@Nonnull ItemOverride value) {
            StringBuilder builder = new StringBuilder();
            builder.append(MoneyValueParser.writeParsable(value.baseCost)).append('|');
            boolean addComma = false;
            for(String entry : value.writeList())
            {
                if(addComma)
                    builder.append(',');
                addComma = true;
                builder.append(entry);
            }
            return StringOption.PARSER.write(builder.toString());
        }
    }

}

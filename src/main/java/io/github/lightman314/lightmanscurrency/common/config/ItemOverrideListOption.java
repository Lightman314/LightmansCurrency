package io.github.lightman314.lightmanscurrency.common.config;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.ItemOverride;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemOverrideListOption extends ListOption<ItemOverride> {

    public static final ConfigParser<ItemOverride> PARSER = new Parser();

    private ItemOverrideListOption(Supplier<List<ItemOverride>> defaultValue) { super(defaultValue); }

    public static ItemOverrideListOption of() { return of(ArrayList::new); }
    public static ItemOverrideListOption of(List<ItemOverride> defaultValue) { return of(() -> defaultValue); }
    public static ItemOverrideListOption of(Supplier<List<ItemOverride>> defaultValue) { return new ItemOverrideListOption(defaultValue); }

    @Override
    protected ConfigParser<ItemOverride> getPartialParser() { return PARSER; }

    @Nullable
    @Override
    protected String bonusComment() { return MoneyValueOption.bonusComment; }

    private static class Parser implements ConfigParser<ItemOverride>
    {
        @Override
        public ItemOverride tryParse(String cleanLine) throws ConfigParsingException {
            String string = StringOption.PARSER.tryParse(cleanLine);
            LightmansCurrency.LogDebug("Parsing '" + string + "' as an ItemOverride");
            String[] split = string.split("\\|");
            //if(split.length <= 1)
            //    throw new ConfigParsingException("Missing '|' splitter");
            if(split.length > 2)
                throw new ConfigParsingException("More than one '|' splitter");
            try {
                MoneyValue baseCost = MoneyValueParser.parse(new StringReader(split[0]),true);
                List<String> list = ImmutableList.of();
                if(split.length > 1)
                {
                    String[] split2 = split[1].split(",");
                    list = ImmutableList.copyOf(split2);
                }
                return new ItemOverride(baseCost,list);
            } catch (CommandSyntaxException | ResourceLocationException e) { throw new ConfigParsingException(e); }
        }
        
        @Override
        public String write(ItemOverride value) {
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
            LightmansCurrency.LogDebug("Writing '" + builder + "' as an ItemOverride");
            return StringOption.PARSER.write(builder.toString());
        }
    }

}

package io.github.lightman314.lightmanscurrency.common.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.BonusForEnchantment;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BonusForEnchantmentListOption extends ListOption<BonusForEnchantment> {

    public static final ConfigParser<BonusForEnchantment> PARSER = new Parser();

    private BonusForEnchantmentListOption(@Nonnull Supplier<List<BonusForEnchantment>> defaultValue) { super(defaultValue); }

    public static BonusForEnchantmentListOption of() { return of(ArrayList::new); }
    public static BonusForEnchantmentListOption of(List<BonusForEnchantment> defaultValue) { return of(() -> defaultValue); }
    public static BonusForEnchantmentListOption of(Supplier<List<BonusForEnchantment>> defaultValue) { return new BonusForEnchantmentListOption(defaultValue); }

    @Override
    protected ConfigParser<BonusForEnchantment> getPartialParser() { return PARSER; }

    @Nullable
    @Override
    protected String bonusComment() { return MoneyValueOption.bonusComment; }

    private static class Parser implements ConfigParser<BonusForEnchantment>
    {
        @Nonnull
        @Override
        public BonusForEnchantment tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            String string = StringOption.PARSER.tryParse(cleanLine);
            String[] split = string.split("\\|");
            if(split.length <= 1)
                throw new ConfigParsingException("Missing '|' splitter");
            if(split.length > 3)
                throw new ConfigParsingException("More than 2 '|' splitters");
            try {
                MoneyValue bonusCost = MoneyValueParser.parse(new StringReader(split[0]),false);
                ResourceLocation enchantment = VersionUtil.parseResource(split[1]);
                int level = 1;
                if(split.length == 3)
                {
                    String levelString = split[2];
                    if(!NumberUtil.IsInteger(levelString))
                        throw new ConfigParsingException(levelString + " is not a valid integer!");
                    level = NumberUtil.GetIntegerValue(levelString,-1);
                }
                return new BonusForEnchantment(bonusCost,enchantment,level);
            } catch (CommandSyntaxException | ResourceLocationException e) { throw new ConfigParsingException(e.getMessage()); }
        }
        @Nonnull
        @Override
        public String write(@Nonnull BonusForEnchantment value) {
            return StringOption.PARSER.write(
                    MoneyValueParser.writeParsable(value.bonusCost) + '|' +
                    value.enchantment + '|' +
                    value.maxLevelCalculation);
        }
    }

}

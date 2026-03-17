package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.ConfiguredTradeMod;

import java.util.function.Supplier;

public class ConfiguredTradeModOption extends ConfigOption<ConfiguredTradeMod> {

    public static final ConfigParser<ConfiguredTradeMod> PARSER = new Parser();

    private ConfiguredTradeModOption(Supplier<ConfiguredTradeMod> defaultValue) { super(defaultValue); }

    public static ConfiguredTradeModOption create(Supplier<ConfiguredTradeMod> defaultValue) { return new ConfiguredTradeModOption(defaultValue); }

    @Override
    protected ConfigParser<ConfiguredTradeMod> getParser() { return PARSER; }

    private static class Parser implements ConfigParser<ConfiguredTradeMod>
    {

        @Override
        public ConfiguredTradeMod tryParse(String cleanLine) throws ConfigParsingException { return ConfiguredTradeMod.tryParse(StringOption.PARSER.tryParse(cleanLine),true); }

        @Override
        public String write(ConfiguredTradeMod value) {
            StringBuilder builder = new StringBuilder();
            value.write(builder);
            return StringOption.PARSER.write(builder.toString());
        }
    }


}

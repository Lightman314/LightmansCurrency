package io.github.lightman314.lightmanscurrency.features.villagers.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.MapLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.features.villagers.mods.VillagerTradeMods;

import java.util.List;
import java.util.function.Supplier;

public class VillagerTradeModsOption extends MapLikeOption<VillagerTradeMods> {

    public static final ConfigParser<VillagerTradeMods> PARSER = new Parser();

    private VillagerTradeModsOption(Supplier<VillagerTradeMods> defaultValue) { super(defaultValue); }

    @Override
    public Pair<Boolean, ConfigParsingException> editMap(String value, String key, boolean isSet) {
        VillagerTradeMods currentValue = this.getCurrentValue();
        try {
            VillagerTradeMods newValue = currentValue.edit(value,key,isSet);
            this.set(newValue);
            return Pair.of(true,null);
        } catch (ConfigParsingException e) { return Pair.of(false,e); }
    }
    
    public static VillagerTradeModsOption create(Supplier<VillagerTradeMods> mods) { return new VillagerTradeModsOption(mods); }

    @Override
    protected ConfigParser<VillagerTradeMods> getParser() { return PARSER; }

    private static final class Parser implements ConfigParser<VillagerTradeMods>
    {
        private final ConfigParser<List<String>> PARSER = ListOption.makeParser(StringOption.PARSER);

        @Override
        public VillagerTradeMods tryParse(String cleanLine) throws ConfigParsingException { return new VillagerTradeMods(PARSER.tryParse(cleanLine)); }

        @Override
        public String write(VillagerTradeMods value) {
            return PARSER.write(value.writeToConfig());
        }
    }

}
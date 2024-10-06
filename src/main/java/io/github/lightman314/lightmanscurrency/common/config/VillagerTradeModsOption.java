package io.github.lightman314.lightmanscurrency.common.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.MapLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMods;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class VillagerTradeModsOption extends MapLikeOption<VillagerTradeMods> {

    public static final ConfigParser<VillagerTradeMods> PARSER = new Parser();

    private VillagerTradeModsOption(@Nonnull Supplier<VillagerTradeMods> defaultValue) { super(defaultValue); }

    @Nonnull
    @Override
    public Pair<Boolean, ConfigParsingException> editMap(@Nonnull String value, @Nonnull String key, boolean isSet) {
        VillagerTradeMods currentValue = this.getCurrentValue();
        try {
            VillagerTradeMods newValue = currentValue.edit(value,key,isSet);
            this.set(newValue);
            return Pair.of(true,null);
        } catch (ConfigParsingException e) { return Pair.of(false,e); }
    }

    @Nonnull
    public static VillagerTradeModsOption create(@Nonnull Supplier<VillagerTradeMods> mods) { return new VillagerTradeModsOption(mods); }

    @Nonnull
    @Override
    protected ConfigParser<VillagerTradeMods> getParser() { return PARSER; }


    private static final class Parser implements ConfigParser<VillagerTradeMods>
    {
        private final ConfigParser<List<String>> PARSER = ListOption.makeParser(StringOption.PARSER);

        @Nonnull
        @Override
        public VillagerTradeMods tryParse(@Nonnull String cleanLine) throws ConfigParsingException { return new VillagerTradeMods(PARSER.tryParse(cleanLine)); }

        @Nonnull
        @Override
        public String write(@Nonnull VillagerTradeMods value) {
            return PARSER.write(value.writeToConfig());
        }
    }

}
package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemOption extends ConfigOption<Item> {

    public static final ConfigParser<Item> PARSER = new Parser(true);
    public static final ConfigParser<Item> PARSER_NO_AIR = new Parser(false);

    private final boolean allowAir;

    protected ItemOption(@Nonnull Supplier<Item> defaultValue, boolean allowAir) { super(defaultValue); this.allowAir = allowAir; }
    @Nonnull
    @Override
    protected ConfigParser<Item> getParser() { return this.allowAir ? PARSER : PARSER_NO_AIR; }

    public static ItemOption create(@Nonnull Supplier<Item> defaultValue) { return new ItemOption(defaultValue,true); }
    public static ItemOption create(@Nonnull Supplier<Item> defaultValue, boolean allowAir) { return new ItemOption(defaultValue,allowAir); }

    @Nullable
    @Override
    protected String bonusComment() { return "Default: " + PARSER.write(this.getDefaultValue()); }

    private static class Parser implements ConfigParser<Item>
    {
        private final boolean allowAir;
        private Parser(boolean allowAir) { this.allowAir = allowAir; }
        @Nonnull
        @Override
        public Item tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            //Manually parse empty string as air
            if(cleanLine.isBlank() && this.allowAir)
                return Items.AIR;
            ResourceLocation itemID = ResourceOption.PARSER.tryParse(cleanLine);
            if(!BuiltInRegistries.ITEM.containsKey(itemID))
                throw new ConfigParsingException("No item found with id " + itemID + "!");
            Item item = BuiltInRegistries.ITEM.get(itemID);
            if(item == Items.AIR && !this.allowAir)
                throw new ConfigParsingException("Air is not an allowed item!");
            return item;
        }
        @Nonnull
        @Override
        public String write(@Nonnull Item value) { return ResourceOption.PARSER.write(BuiltInRegistries.ITEM.getKey(value)); }
    }

}

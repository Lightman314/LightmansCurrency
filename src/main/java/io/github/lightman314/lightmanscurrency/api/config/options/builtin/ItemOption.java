package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemOption extends ConfigOption<Item> {

    public final Predicate<Item> filter;

    public static ConfigParser<Item> createParser(Predicate<Item> filter) { return new Parser(filter); }

    private final Parser parser;
    protected ItemOption(Supplier<Item> defaultValue, Predicate<Item> filter) { super(defaultValue); this.filter = filter; this.parser = new Parser(this.filter); }
    @Override
    protected ConfigParser<Item> getParser() { return this.parser; }

    public static ItemOption create(Supplier<Item> defaultValue) { return new ItemOption(defaultValue,i -> true); }
    public static ItemOption createNoAir(Supplier<Item> defaultValue) { return new ItemOption(defaultValue,i -> i != Items.AIR); }
    public static ItemOption createNoAir(Supplier<Item> defaultValue,Predicate<Item> filter) { return new ItemOption(defaultValue,filter); }

    @Override
    public boolean allowedValue(Item newValue) { return this.filter.test(newValue); }

    @Nullable
    @Override
    protected String bonusComment() { return "Default: " + this.parser.write(this.getDefaultValue()); }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_DEFAULT.get(new ItemStack(this.getDefaultValue()).getHoverName()); }

    private static class Parser implements ConfigParser<Item>
    {
        private final Predicate<Item> filter;
        private Parser(Predicate<Item> filter) { this.filter = filter; }
        
        @Override
        public Item tryParse(String cleanLine) throws ConfigParsingException {
            //Manually parse empty string as air
            if(cleanLine.isBlank() && this.filter.test(Items.AIR))
                return Items.AIR;
            ResourceLocation itemID = ResourceOption.PARSER.tryParse(cleanLine);
            if(!BuiltInRegistries.ITEM.containsKey(itemID))
                throw new ConfigParsingException("No item found with id " + itemID + "!");
            Item item = BuiltInRegistries.ITEM.get(itemID);
            if(!this.filter.test(item))
                throw new ConfigParsingException(new ItemStack(item).getHoverName().getString() + " is not an allowed item!");
            return item;
        }
        
        @Override
        public String write(Item value) { return ResourceOption.PARSER.write(BuiltInRegistries.ITEM.getKey(value)); }
    }

}

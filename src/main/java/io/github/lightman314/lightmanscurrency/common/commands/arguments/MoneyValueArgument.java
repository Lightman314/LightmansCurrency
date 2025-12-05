package io.github.lightman314.lightmanscurrency.common.commands.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MoneyValueArgument implements ArgumentType<MoneyValue> {

    private final HolderLookup<Item> items;

    private MoneyValueArgument(HolderLookup<Item> items) { this.items = items; }

    public static MoneyValueArgument argument(CommandBuildContext context) { return new MoneyValueArgument(context.holderLookup(ForgeRegistries.ITEMS.getRegistryKey())); }

    public static MoneyValue getMoneyValue(CommandContext<CommandSourceStack> commandContext, String name) throws CommandSyntaxException {
        return commandContext.getArgument(name, MoneyValue.class);
    }

    @Override
    public MoneyValue parse(StringReader reader) throws CommandSyntaxException { return MoneyValueParser.parse(reader, false); }

    @Nonnull
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {

        String arg = builder.getRemaining();
        if(arg.contains(";"))
        {
            String[] split = arg.split(";",2);
            String prefix = split[0];
            String trail = split.length > 1 ? split[1] : "";
            for(CurrencyType type : MoneyAPI.getApi().AllCurrencyTypes())
            {
                MoneyValueParser parser = type.getValueParser();
                if(parser != null && parser.prefix.equals(prefix))
                    return parser.listSuggestions(context, builder, trail, this.items);
            }
            return Suggestions.empty();
        }
        else
        {
            for(CurrencyType type : MoneyAPI.getApi().AllCurrencyTypes())
            {
                MoneyValueParser parser = type.getValueParser();
                if(parser != null && parser.prefix.startsWith(builder.getRemainingLowerCase()))
                    builder.suggest(parser.prefix + ";");
            }
            return builder.buildFuture();
        }
    }

    public Collection<String> getExamples() {
        List<String> examples = new ArrayList<>();
        for(CurrencyType type : MoneyAPI.getApi().AllCurrencyTypes())
        {
            MoneyValueParser parser = type.getValueParser();
            if(parser != null)
                parser.addExamples(examples);
        }
        return ImmutableList.copyOf(examples);
    }

}
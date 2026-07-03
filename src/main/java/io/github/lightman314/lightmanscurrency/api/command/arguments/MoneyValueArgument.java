package io.github.lightman314.lightmanscurrency.api.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class MoneyValueArgument implements ArgumentType<MoneyValue> {

    private final HolderLookup<Item> items;
    private MoneyValueArgument(HolderLookup<Item> items) { this.items = items; }

    public static MoneyValueArgument argument(CommandBuildContext context) { return new MoneyValueArgument(context.lookupOrThrow(Registries.ITEM)); }

    public static MoneyValue getMoneyValue(CommandContext<CommandSourceStack> commandContext,String name) throws CommandSyntaxException {
        return commandContext.getArgument(name,MoneyValue.class);
    }

    @Override
    public MoneyValue parse(StringReader reader) throws CommandSyntaxException { return MoneyValueParser.parse(reader,false,false); }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String arg = builder.getRemaining();
        if(arg.contains(";"))
        {
            String[] split = arg.split(";",2);
            String prefix = split[0];
            String trail = split.length > 1 ? split[1] : "";
            for(MoneyValueType<?> type : LCRegistries.Money.VALUE_TYPE)
            {
                MoneyValueParser<?> parser = type.parser();
                if(parser.prefix.equals(prefix))
                    return parser.listSuggestions(context,builder,trail,this.items);
            }
            return Suggestions.empty();
        }
        else {
            for(MoneyValueType<?> type : LCRegistries.Money.VALUE_TYPE)
            {
                MoneyValueParser<?> parser = type.parser();
                if(parser.prefix.startsWith(builder.getRemainingLowerCase()))
                    builder.suggest(parser.prefix + ";");
            }
            return builder.buildFuture();
        }
    }

}
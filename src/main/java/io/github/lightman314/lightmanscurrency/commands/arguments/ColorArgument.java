package io.github.lightman314.lightmanscurrency.commands.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.core.variants.Color;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ColorArgument implements ArgumentType<Integer> {

    private static final SimpleCommandExceptionType ERROR_NOT_VALID = new SimpleCommandExceptionType(EasyText.translatable("command.argument.color.invalid"));

    private ColorArgument() {}

    public static ColorArgument argument() { return new ColorArgument(); }

    public static int getColor(CommandContext<CommandSourceStack> commandContext, String name) {
        return commandContext.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String color = reader.readUnquotedString();
        if(color.startsWith("0x"))
        {
            //Read Hex Color
            return Integer.decode(color);
        }
        else if(isNumerical(color))
        {
            //Read Raw Integer
            return Integer.parseInt(color);
        }
        //Read the rest from the color enum list
        Color c = Color.getFromPrettyName(color);
        if(c != null)
            return c.hexColor;
        throw ERROR_NOT_VALID.createWithContext(reader);
    }

    private static boolean isNumerical(String string) {
        for(int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);
            if(c < '0' || c > '9')
                return false;
        }
        return true;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for(Color c : Color.values())
            builder.suggest(c.toString());
        builder.suggest("0xFFFFFF");
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() { return ImmutableList.of("0xFFFFFF", "16777215", "WHITE"); }

}
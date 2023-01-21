package io.github.lightman314.lightmanscurrency.commands.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.lightman314.lightmanscurrency.core.variants.Color;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class ColorArgument implements ArgumentType<Integer> {

    private static final SimpleCommandExceptionType ERROR_NOT_VALID = new SimpleCommandExceptionType(Component.translatable("command.argument.color.invalid"));

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
        //Manually check for light blue/gray underscore variants
        if(color.equalsIgnoreCase("light_blue"))
            return Color.LIGHTBLUE.hexColor;
        if(color.equalsIgnoreCase("light_gray"))
            return Color.LIGHTGRAY.hexColor;
        //Read the rest from the color enum list
        Color c = EnumUtil.enumFromString(color, Color.values(), null);
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

    public Collection<String> getExamples() { return ImmutableList.of("0xFFFFFF", "16777215", "WHITE"); }

}

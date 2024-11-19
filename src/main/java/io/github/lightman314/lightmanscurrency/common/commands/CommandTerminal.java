package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandTerminal {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command =
                Commands.literal("lcterminal")
                        .requires(s -> s.isPlayer() && LCConfig.SERVER.openTerminalCommand.get())
                        .executes(CommandTerminal::openTerminal);

        dispatcher.register(command);
    }

    private static int openTerminal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if(QuarantineAPI.IsDimensionQuarantined(player))
        {
            context.getSource().sendFailure(LCText.MESSAGE_DIMENSION_QUARANTINED_TERMINAL.get());
            return 0;
        }
        TerminalMenuProvider.OpenMenu(player,SimpleValidator.NULL);
        return 1;
    }

}

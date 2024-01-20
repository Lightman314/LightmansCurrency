package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.network.message.config.SPacketReloadConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandConfig {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> configReloadCommand
            = Commands.literal("lcconfig")
                .then(Commands.literal("reload")
                        .executes(CommandConfig::reload));

        dispatcher.register(configReloadCommand);
    }

    static int reload(CommandContext<CommandSourceStack> commandContext) {
        int result = 0;
        boolean involveAdmins = false;
        if(commandContext.getSource().hasPermission(2))
        {
            involveAdmins = true;
            ConfigFile.reloadFiles();
            result++;
        }
        ServerPlayer player = commandContext.getSource().getPlayer();
        if(player != null)
        {
            SPacketReloadConfig.INSTANCE.sendTo(player);
            result++;
        }
        if(result > 0)
            EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcconfig"), involveAdmins);
        return result;
    }

}

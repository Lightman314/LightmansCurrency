package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.message.config.SPacketReloadConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class CommandConfig {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> configReloadCommand
            = Commands.literal("lcconfig")
                .then(Commands.literal("reload")
                        .executes(CommandConfig::reload))
                .then(configEditCommands());

        dispatcher.register(configReloadCommand);
    }

    static int reload(CommandContext<CommandSourceStack> commandContext) {
        int result = 0;
        boolean involveAdmins = false;
        if(commandContext.getSource().hasPermission(2))
        {
            involveAdmins = true;
            ConfigFile.reloadServerFiles();
            //Reload normal data, as there's no point in not also reloading the non-standard config files
            TraderSaveData.ReloadPersistentTraders();
            CoinAPI.reloadMoneyDataFromFile();
            result++;
        }
        ServerPlayer player = commandContext.getSource().getPlayer();
        if(player != null)
        {
            SPacketReloadConfig.INSTANCE.sendTo(player);
            result++;
        }
        if(result > 0)
            EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcconfig.reload"), involveAdmins);
        return result;
    }

    private static ArgumentBuilder<CommandSourceStack,?> configEditCommands()
    {
        LiteralArgumentBuilder<CommandSourceStack> edit = Commands.literal("edit");
        for(ConfigFile file : ConfigFile.getAvailableFiles())
        {
            LiteralArgumentBuilder<CommandSourceStack> fileSection = Commands.literal(file.getFileName());
            edit.then(fileSection).requires((stack) -> file.isClientOnly() || stack.hasPermission(2));
            file.getAllOptions().forEach((key,option) ->
                fileSection.then(Commands.literal(key))
                        .then(Commands.argument("value", StringArgumentType.greedyString()))
                        .executes(context -> commandEdit(context, file, key))
            );
        }
        return edit;
    }

    static int commandEdit(CommandContext<CommandSourceStack> commandContext, ConfigFile file, String configOption) {
        String input = StringArgumentType.getString(commandContext, "value");
        if(file.isClientOnly())
        {
            //Send packet to client to edit value
            return 1;
        }
        Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
        if(optionMap.containsKey(configOption))
        {
            ConfigOption<?> option = optionMap.get(configOption);
            ConfigParsingException e = option.load(configOption, input, ConfigOption.LoadSource.FILE);
            if(e != null)
            {
                EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcconfig.edit.fail.parse", e.getMessage()));
                return 0;
            }
            EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcconfig.edit.success", configOption, input), true);
            return 1;
        }
        EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcconfig.edit.fail.missing"));
        return 0;
    }

}

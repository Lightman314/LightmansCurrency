package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.message.config.*;
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
                .then(configEditCommands())
                .then(configResetCommands())
                .then(configViewCommands());

        dispatcher.register(configReloadCommand);
    }

    static int reload(CommandContext<CommandSourceStack> commandContext) {
        int result = 0;
        boolean involveAdmins = false;
        if(commandContext.getSource().hasPermission(2))
        {
            involveAdmins = true;
            //Reload coin data before reloading config options
            CoinAPI.API.ReloadCoinDataFromFile();
            //Reload config files
            ConfigFile.reloadServerFiles();
            //Reload Persistent Traders
            TraderSaveData.ReloadPersistentTraders();
            result++;
        }
        ServerPlayer player = commandContext.getSource().getPlayer();
        if(player != null)
        {
            SPacketReloadConfig.INSTANCE.sendTo(player);
            result++;
        }
        if(result > 0)
            EasyText.sendCommandSucess(commandContext.getSource(), LCText.COMMAND_CONFIG_RELOAD.get(), involveAdmins);
        return result;
    }

    private static ArgumentBuilder<CommandSourceStack,?> configEditCommands()
    {
        LiteralArgumentBuilder<CommandSourceStack> edit = Commands.literal("edit");
        for(ConfigFile file : ConfigFile.getAvailableFiles())
        {
            LiteralArgumentBuilder<CommandSourceStack> fileSection = Commands.literal(file.getFileName())
                    .requires((stack) -> file.isClientOnly() ? stack.isPlayer() : stack.hasPermission(2));
            file.getAllOptions().forEach((key,option) -> {
                if (option instanceof ListOption<?>)
                {
                    //Special list editing commands
                    fileSection.then(Commands.literal(key)
                            .then(Commands.literal("add")
                                    .then(Commands.argument("value", StringArgumentType.greedyString())
                                            .executes(context -> commandEditList(context, file, key, -1, true))))
                            .then(Commands.literal("replace")
                                    .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                            .then(Commands.argument("value", StringArgumentType.greedyString())
                                                    .executes(context -> commandEditList(context, file, key, IntegerArgumentType.getInteger(context, "index"), true)))))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                            .executes(context -> commandEditList(context, file, key, IntegerArgumentType.getInteger(context, "index"), false))))
                    );
                }
                else
                {
                    //Normal editing commands
                    fileSection.then(Commands.literal(key)
                            .then(Commands.argument("value", StringArgumentType.greedyString())
                                    .executes(context -> commandEdit(context, file, key))));
                }
            });
            edit.then(fileSection);
        }
        return edit;
    }

    static int commandEdit(CommandContext<CommandSourceStack> commandContext, ConfigFile file, String configOption) throws CommandSyntaxException {
        String input = StringArgumentType.getString(commandContext, "value");
        if(file.isClientOnly())
        {
            //Send packet to client to edit value
            new SPacketEditConfig(file.getFileName(), configOption, input).sendTo(commandContext.getSource().getPlayerOrException());
            return 1;
        }
        Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
        if(optionMap.containsKey(configOption))
        {
            ConfigOption<?> option = optionMap.get(configOption);
            Pair<Boolean,ConfigParsingException> result = option.load(input, ConfigOption.LoadSource.COMMAND);
            if(!result.getFirst())
            {
                EasyText.sendCommandFail(commandContext.getSource(), LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()));
                return 0;
            }
            EasyText.sendCommandSucess(commandContext.getSource(), LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(option.getName(), input), true);
            return 1;
        }
        EasyText.sendCommandFail(commandContext.getSource(), LCText.COMMAND_CONFIG_FAIL_MISSING.get(configOption));
        return 0;
    }

    static int commandEditList(CommandContext<CommandSourceStack> commandContext, ConfigFile file, String configOption, int listIndex, boolean isEdit) throws CommandSyntaxException {
        String input;
        if(isEdit)
            input = StringArgumentType.getString(commandContext, "value");
        else
            input = "";
        if(file.isClientOnly())
        {
            //Send packet to client to edit value
            new SPacketEditListConfig(file.getFileName(), configOption, input, listIndex, isEdit).sendTo(commandContext.getSource().getPlayerOrException());
            return 1;
        }
        Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
        if(optionMap.containsKey(configOption) && optionMap.get(configOption) instanceof ListOption<?> option)
        {
            Pair<Boolean,ConfigParsingException> result = option.editList(input, listIndex, isEdit);
            if(!result.getFirst())
            {
                EasyText.sendCommandFail(commandContext.getSource(), LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()));
                return 0;
            }
            if(!isEdit)
                EasyText.sendCommandSucess(commandContext.getSource(), LCText.COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS.get(configOption + "[" + listIndex + "]"), true);
            if(listIndex < 0)
                listIndex = option.get().size() - 1;
            EasyText.sendCommandSucess(commandContext.getSource(), LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(configOption + "[" + listIndex + "]", input), true);
            return 1;
        }
        EasyText.sendCommandFail(commandContext.getSource(), LCText.COMMAND_CONFIG_FAIL_MISSING.get(configOption));
        return 0;
    }

    public static ArgumentBuilder<CommandSourceStack,?> configResetCommands()
    {
        LiteralArgumentBuilder<CommandSourceStack> view = Commands.literal("reset");
        for(ConfigFile file : ConfigFile.getAvailableFiles())
        {
            LiteralArgumentBuilder<CommandSourceStack> fileSection = Commands.literal(file.getFileName())
                    .requires((stack) -> file.isClientOnly() ? stack.isPlayer() : stack.hasPermission(2));
            file.getAllOptions().forEach((key,option) ->
                    fileSection.then(Commands.literal(key)
                            .executes(context -> commandReset(context, file, key)))
            );
            view.then(fileSection);
        }
        return view;
    }

    static int commandReset(CommandContext<CommandSourceStack> commandContext, ConfigFile file, String configOption) throws CommandSyntaxException{
        if(file.isClientOnly())
        {
            //Send packet to client to edit value
            new SPacketResetConfig(file.getFileName(), configOption).sendTo(commandContext.getSource().getPlayerOrException());
            return 1;
        }
        Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
        if(optionMap.containsKey(configOption))
        {
            ConfigOption<?> option = optionMap.get(configOption);
            option.setToDefault();
            EasyText.sendCommandSucess(commandContext.getSource(), LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(option.getName(), option.write()), true);
            return 1;
        }
        EasyText.sendCommandFail(commandContext.getSource(), LCText.COMMAND_CONFIG_FAIL_MISSING.get(configOption));
        return 0;
    }

    private static ArgumentBuilder<CommandSourceStack,?> configViewCommands()
    {
        LiteralArgumentBuilder<CommandSourceStack> view = Commands.literal("view");
        for(ConfigFile file : ConfigFile.getAvailableFiles())
        {
            LiteralArgumentBuilder<CommandSourceStack> fileSection = Commands.literal(file.getFileName())
                    .requires(stack -> stack.isPlayer() || !file.isClientOnly());
            file.getAllOptions().forEach((key,option) ->
                    fileSection.then(Commands.literal(key)
                            .executes(context -> commandView(context, file, key)))
            );
            view.then(fileSection);
        }
        return view;
    }

    static int commandView(CommandContext<CommandSourceStack> commandContext, ConfigFile file, String configOption) throws CommandSyntaxException{
        if(file.isClientOnly())
        {
            //Send packet to client to edit value
            new SPacketViewConfig(file.getFileName(), configOption).sendTo(commandContext.getSource().getPlayerOrException());
            return 1;
        }
        Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
        if(optionMap.containsKey(configOption))
        {
            ConfigOption<?> option = optionMap.get(configOption);
            EasyText.sendCommandSucess(commandContext.getSource(), LCText.COMMAND_CONFIG_VIEW.get(option.getName()), false);
            EasyText.sendCommandSucess(commandContext.getSource(), EasyText.literal(option.write()), false);
            return 1;
        }
        EasyText.sendCommandFail(commandContext.getSource(), LCText.COMMAND_CONFIG_FAIL_MISSING.get(configOption));
        return 0;
    }

}

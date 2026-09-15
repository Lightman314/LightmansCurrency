package io.github.lightman314.lightmanscurrency.features.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.config.ConfigReloadable;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class LCConfigCommand {
    private LCConfigCommand() {}

    public static final TextEntry RELOAD = TextEntry.command(LCApi.MODID,"lcconfig.reload");
    public static final TextEntry RELOAD_FILE = TextEntry.command(LCApi.MODID,"lcconfig.reload.file");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("lcconfig")
                .then(configReloadCommands());

        dispatcher.register(command);

    }

    private static ArgumentBuilder<CommandSourceStack,?> configReloadCommands() {
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload")
                .executes(LCConfigCommand::reloadAll);
        for(ConfigReloadable reloadable : LCApi.getConfigAPI().getReloadablesInOrder()) {
            reload.then(Commands.literal(reloadable.getID().toString())
                    .requires(reloadable::canReload)
                    .executes(context -> reloadFile(context,reloadable)));
        }
        return reload;
    }

    private static int reloadAll(CommandContext<CommandSourceStack> context) {
        int result = 0;
        boolean alertAdmins = false;
        CommandSourceStack stack = context.getSource();
        for(ConfigReloadable reloadable : LCApi.getConfigAPI().getReloadablesInOrder()) {
            try{
                if(reloadable.canReload(stack)) {
                    reloadable.onCommandReload(stack);
                    alertAdmins = alertAdmins || reloadable.alertAdmins();
                    result++;
                }
            }catch (CommandSyntaxException e) {
                LightmansCurrency.LogWarning("Error reloading " + reloadable.getID() + " config file from command!",e);
            }
        }
        if(result > 0)
            stack.sendSuccess(RELOAD.get(result)::copy,alertAdmins);

        return result;
    }

    private static int reloadFile(CommandContext<CommandSourceStack> context,ConfigReloadable reloadable) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        if(reloadable.canReload(stack)) {
            reloadable.onCommandReload(stack);
            stack.sendSuccess(RELOAD_FILE.get(reloadable.getID())::copy,reloadable.alertAdmins());
            return 1;
        }
        return 0;
    }

}
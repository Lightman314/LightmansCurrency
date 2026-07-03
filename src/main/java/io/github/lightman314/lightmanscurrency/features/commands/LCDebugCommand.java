package io.github.lightman314.lightmanscurrency.features.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.command.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.network.message.debug.SPacketDebugTraderData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;

public final class LCDebugCommand {

    private LCDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("lcdebug")
                .requires(LCCommandSetup.requiresLevel(PermissionLevel.MODERATORS))
                .then(Commands.literal("trader")
                        .then(Commands.argument("traderID",TraderArgument.traderWithPersistent())
                            .executes(LCDebugCommand::debugTrader))));
    }

    private static int debugTrader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        TraderData trader = TraderArgument.getTrader(context,"traderID");
        int result = 0;
        if(trader != null)
        {
            DataContext<JsonElement> encoder = DataContext.createJson(source.registryAccess());
            LightmansCurrency.LogInfo("Server Copy of Trader #" + trader.getID() + ":\n" + JsonHelper.PRETTY_GSON.toJson(encoder.write(trader,TraderData.CODEC)));
            source.sendSuccess(() -> Component.literal("Server data for trader #" + trader.getID() + " has been printed to the logs!"),false);
            result++;
        }
        else
            source.sendSuccess(() -> Component.literal("Trader #" + trader.getID() + " does not exist on the server."),false);
        if(source.isPlayer())
        {
            new SPacketDebugTraderData(trader.getID()).sendTo(source.getPlayer());
            result++;
        }
        return result;
    }

}

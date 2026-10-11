package io.github.lightman314.lightmanscurrency.features.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.command.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.CodecInteractionHelper;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.network.message.debug.SPacketDebugBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.debug.SPacketDebugTraderData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;

public final class LCDebugCommand {

    private LCDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("lcdebug")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("trader")
                        .then(Commands.argument("traderID",TraderArgument.traderWithPersistent())
                            .executes(LCDebugCommand::debugTrader)))
                .then(Commands.literal("bank")
                        .then(Commands.literal("player")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(LCDebugCommand::debugPlayerAccount)))));
    }

    private static int debugTrader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        TraderData trader = TraderArgument.getTrader(context,"traderID");
        int result = 0;
        CodecInteractionHelper<JsonElement> encoder = CodecInteractionHelper.createJson(source.registryAccess());
        LightmansCurrency.LogInfo("Server Copy of Trader #" + trader.getID() + ":\n" + JsonHelper.PRETTY_GSON.toJson(encoder.write(trader,TraderData.CODEC)));
        source.sendSuccess(() -> Component.literal("Server data for trader #" + trader.getID() + " has been printed to the logs!"),false);
        result++;
        if(source.isPlayer())
        {
            new SPacketDebugTraderData(trader.getID()).sendTo(source.getPlayer());
            result++;
        }
        return result;
    }

    private static int debugPlayerAccount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context,"player");
        BankAccount account = PlayerBankReference.of(player).get();
        int result = 0;
        CodecInteractionHelper<JsonElement> encoder = CodecInteractionHelper.createJson(source.registryAccess());
        LightmansCurrency.LogInfo("Server Copy of " + account.getName().getContents() + ":\n" + JsonHelper.PRETTY_GSON.toJson(encoder.write(account,BankAccount.CODEC)));
        source.sendSuccess(() -> Component.literal("Server data for " + account.getName().getString() + " has been printed to the logs!"),false);
        result++;
        if(source.isPlayer()) {
            new SPacketDebugBankAccount(PlayerBankReference.of(player)).sendTo(source.getPlayer());
            result++;
        }
        return result;
    }

}

package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TradeIDArgument;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTradeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerPlayer;

public class CommandPlayerTrading {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> requestTradeCommand
                = Commands.literal("lctrade")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(CommandPlayerTrading::requestPlayerTrade));

        LiteralArgumentBuilder<CommandSourceStack> acceptTradeCommand
                = Commands.literal("lctradeaccept")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.argument("tradeID", TradeIDArgument.argument())
                        .executes(CommandPlayerTrading::acceptPlayerTrade));

        dispatcher.register(requestTradeCommand);
        dispatcher.register(acceptTradeCommand);

    }

    private static int requestPlayerTrade(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer host = context.getSource().getPlayerOrException();
        ServerPlayer guest = EntityArgument.getPlayer(context, "player");
        CommandSourceStack source = context.getSource();

        if(guest == host)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lctrade.self"));
            return 0;
        }

        int tradeID = PlayerTradeManager.CreateNewTrade(host, guest);

        host.sendSystemMessage(EasyText.translatable("command.lightmanscurrency.lctrade.host.notify", guest.getName()));
        guest.sendSystemMessage(EasyText.translatable("command.lightmanscurrency.lctrade.guest.notify", host.getName(), EasyText.translatable("command.lightmanscurrency.lctrade.guest.notify.here").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN).withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lctradeaccept " + tradeID)))).withStyle(ChatFormatting.GOLD));

        return 1;
    }

    private static int acceptPlayerTrade(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer guest = context.getSource().getPlayerOrException();
        int tradeID = TradeIDArgument.getTradeID(context,"tradeID");
        CommandSourceStack source = context.getSource();

        PlayerTrade trade = PlayerTradeManager.GetTrade(tradeID);
        if(trade != null && trade.isGuest(guest))
        {
            int rangeResult = trade.isGuestInRange(guest);
            if(rangeResult > 0)
            {
                EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lctradeaccept.fail." + rangeResult, PlayerTrade.enforceDistance()));
                return 0;
            }
            if(trade.requestAccepted(guest))
                return 1;
            else
            {
                EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lctradeaccept.error"));
                return 0;
            }
        }
        else
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lctradeaccept.notfound"));
            return 0;
        }
    }

}

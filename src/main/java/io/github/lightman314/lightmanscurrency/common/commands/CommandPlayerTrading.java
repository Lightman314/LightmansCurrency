package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TradeIDArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTradeManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.UUID;

public class CommandPlayerTrading {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> requestTradeCommand
                = Commands.literal("lctrade")
                .requires(stack -> stack.getEntity() instanceof PlayerEntity)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(CommandPlayerTrading::requestPlayerTrade));

        LiteralArgumentBuilder<CommandSource> acceptTradeCommand
                = Commands.literal("lctradeaccept")
                .requires(stack -> stack.getEntity() instanceof PlayerEntity)
                .then(Commands.argument("tradeID", TradeIDArgument.argument())
                        .executes(CommandPlayerTrading::acceptPlayerTrade));

        dispatcher.register(requestTradeCommand);
        dispatcher.register(acceptTradeCommand);

    }

    private static int requestPlayerTrade(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity host = context.getSource().getPlayerOrException();
        ServerPlayerEntity guest = EntityArgument.getPlayer(context, "player");

        if(guest == host)
        {
            context.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lctrade.self"));
            return 0;
        }

        int tradeID = PlayerTradeManager.CreateNewTrade(host, guest);

        host.sendMessage(EasyText.translatable("command.lightmanscurrency.lctrade.host.notify", guest.getName()), new UUID(0,0));
        guest.sendMessage(EasyText.translatable("command.lightmanscurrency.lctrade.guest.notify", host.getName(), EasyText.translatable("command.lightmanscurrency.lctrade.guest.notify.here").withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GREEN).withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lctradeaccept " + tradeID)))).withStyle(TextFormatting.GOLD), new UUID(0,0));

        return 1;
    }

    private static int acceptPlayerTrade(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity guest = context.getSource().getPlayerOrException();
        int tradeID = TradeIDArgument.getTradeID(context,"tradeID");

        PlayerTrade trade = PlayerTradeManager.GetTrade(tradeID);
        if(trade != null && trade.isGuest(guest))
        {
            if(trade.requestAccepted(guest))
                return 1;
            else
            {
                context.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lctradeaccept.error"));
                return 0;
            }
        }
        else
        {
            context.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lctradeaccept.notfound"));
            return 0;
        }
    }

}
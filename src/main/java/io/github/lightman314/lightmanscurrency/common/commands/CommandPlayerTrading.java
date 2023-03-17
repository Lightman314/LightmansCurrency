package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TradeIDArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTradeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class CommandPlayerTrading {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> requestTradeCommand
                = Commands.literal("lctrade")
                .requires(stack -> stack.getEntity() instanceof Player)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(CommandPlayerTrading::requestPlayerTrade));

        LiteralArgumentBuilder<CommandSourceStack> acceptTradeCommand
                = Commands.literal("lctradeaccept")
                .requires(stack -> stack.getEntity() instanceof Player)
                .then(Commands.argument("tradeID", TradeIDArgument.argument())
                        .executes(CommandPlayerTrading::acceptPlayerTrade));

        dispatcher.register(requestTradeCommand);
        dispatcher.register(acceptTradeCommand);

    }

    private static int requestPlayerTrade(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer host = context.getSource().getPlayerOrException();
        ServerPlayer guest = EntityArgument.getPlayer(context, "player");

        if(guest == host)
        {
            context.getSource().sendFailure(new TranslatableComponent("command.lightmanscurrency.lctrade.self"));
            return 0;
        }

        int tradeID = PlayerTradeManager.CreateNewTrade(host, guest);

        host.sendMessage(new TranslatableComponent("command.lightmanscurrency.lctrade.host.notify", guest.getName()), new UUID(0,0));
        guest.sendMessage(new TranslatableComponent("command.lightmanscurrency.lctrade.guest.notify", host.getName(), new TranslatableComponent("command.lightmanscurrency.lctrade.guest.notify.here").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN).withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lctradeaccept " + tradeID)))).withStyle(ChatFormatting.GOLD), new UUID(0,0));

        return 1;
    }

    private static int acceptPlayerTrade(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer guest = context.getSource().getPlayerOrException();
        int tradeID = TradeIDArgument.getTradeID(context,"tradeID");

        PlayerTrade trade = PlayerTradeManager.GetTrade(tradeID);
        if(trade != null && trade.isGuest(guest))
        {
            int rangeResult = trade.isGuestInRange(guest);
            if(rangeResult > 0)
            {
                context.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lctradeaccept.fail." + rangeResult, PlayerTrade.enforceDistance()));
                return 0;
            }
            if(trade.requestAccepted(guest))
                return 1;
            else
            {
                context.getSource().sendFailure(new TranslatableComponent("command.lightmanscurrency.lctradeaccept.error"));
                return 0;
            }
        }
        else
        {
            context.getSource().sendFailure(new TranslatableComponent("command.lightmanscurrency.lctradeaccept.notfound"));
            return 0;
        }
    }

}
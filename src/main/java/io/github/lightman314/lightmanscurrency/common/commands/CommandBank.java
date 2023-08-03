package io.github.lightman314.lightmanscurrency.common.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.CoinValueArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.secrets.Secret;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CommandBank {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {

        LiteralArgumentBuilder<CommandSourceStack> bankCommand
                = Commands.literal("lcbank")
                .requires(stack -> stack.hasPermission(2) || Secret.hasSecretAccess(stack))
                .then(Commands.literal("give")
                        .then(Commands.literal("allPlayers")
                                .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                        .executes(CommandBank::giveAllPlayers)))
                        .then(Commands.literal("allTeams")
                                .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                        .executes(CommandBank::giveAllTeams)))
                        .then(Commands.literal("players")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                                .executes(CommandBank::givePlayers))))
                        .then(Commands.literal("team")
                                .then(Commands.argument("teamID", LongArgumentType.longArg(0))
                                        .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                                .executes(CommandBank::giveTeam)))))
                .then(Commands.literal("take")
                        .then(Commands.literal("allPlayers")
                                .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                        .executes(CommandBank::takeAllPlayers)))
                        .then(Commands.literal("allTeams")
                                .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                        .executes(CommandBank::takeAllTeams)))
                        .then(Commands.literal("players")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                                .executes(CommandBank::takePlayers))))
                        .then(Commands.literal("team")
                                .then(Commands.argument("teamID", LongArgumentType.longArg(0))
                                        .then(Commands.argument("amount", CoinValueArgument.argument(context))
                                                .executes(CommandBank::takeTeam)))));

        dispatcher.register(bankCommand);

    }

    static int giveAllPlayers(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), BankSaveData.GetPlayerBankAccounts(), amount);
    }

    static int giveAllTeams(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), TeamSaveData.GetAllTeams(false).stream().filter(Team::hasBankAccount).map(Team::getReference).toList(), amount);
    }

    static int givePlayers(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext, "amount");
        return giveTo(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "players").stream().map(BankAccount::GenerateReference).toList(), amount);
    }

    static int giveTeam(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        long teamID = LongArgumentType.getLong(commandContext, "teamID");
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext, "amount");
        Team team = TeamSaveData.GetTeam(false, teamID);
        if(team == null)
        {
            commandContext.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lcbank.team.noteam", teamID));
            return 0;
        }
        else if(!team.hasBankAccount())
        {
            commandContext.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lcbank.team.nobank", teamID));
            return 0;
        }
        return giveTo(commandContext.getSource(), Lists.newArrayList(team.getReference()), amount);
    }

    static int giveTo(CommandSourceStack source, List<BankAccount.AccountReference> accounts, CoinValue amount)
    {
        int count = 0;
        Component bankName = null;
        for(BankAccount.AccountReference account : accounts)
        {
            if(BankAccount.ServerGiveCoins(account.get(), amount))
            {
                count++;
                if(count == 1)
                    bankName = account.get().getName();
            }

        }
        if(count < 1)
            source.sendFailure(EasyText.translatable("command.lightmanscurrency.lcbank.give.fail"));
        else
        {
            if(count == 1)
                source.sendSuccess(EasyText.translatable("command.lightmanscurrency.lcbank.give.success.single", amount.getComponent("NULL"), bankName), true);
            else
                source.sendSuccess(EasyText.translatable("command.lightmanscurrency.lcbank.give.success", amount.getComponent("NULL"), count), true);
        }
        return count;
    }

    static int takeAllPlayers(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), BankSaveData.GetPlayerBankAccounts(), amount);
    }

    static int takeAllTeams(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), TeamSaveData.GetAllTeams(false).stream().filter(Team::hasBankAccount).map(Team::getReference).toList(), amount);
    }

    static int takePlayers(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext, "amount");
        return takeFrom(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "players").stream().map(BankAccount::GenerateReference).toList(), amount);
    }

    static int takeTeam(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        long teamID = LongArgumentType.getLong(commandContext, "teamID");
        CoinValue amount = CoinValueArgument.getCoinValue(commandContext, "amount");
        Team team = TeamSaveData.GetTeam(false, teamID);
        if(team == null)
        {
            commandContext.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lcbank.team.noteam", teamID));
            return 0;
        }
        else if(!team.hasBankAccount())
        {
            commandContext.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.lcbank.team.nobank", teamID));
            return 0;
        }
        return takeFrom(commandContext.getSource(), Lists.newArrayList(team.getReference()), amount);
    }

    static int takeFrom(CommandSourceStack source, List<BankAccount.AccountReference> accounts, CoinValue amount)
    {
        int count = 0;
        Component bankName = null;
        CoinValue largestAmount = CoinValue.EMPTY;
        for(BankAccount.AccountReference account : accounts)
        {
            Pair<Boolean,CoinValue> result = BankAccount.ServerTakeCoins(account.get(), amount);
            if(result.getFirst())
            {
                count++;
                if(count == 1)
                    bankName = account.get().getName();
                if(result.getSecond().getValueNumber() > largestAmount.getValueNumber())
                    largestAmount = result.getSecond();
            }
        }
        if(count < 1)
            source.sendFailure(EasyText.translatable("command.lightmanscurrency.lcbank.take.fail"));
        else
        {
            if(count == 1)
                source.sendSuccess(EasyText.translatable("command.lightmanscurrency.lcbank.take.success.single", largestAmount.getComponent("NULL"), bankName), true);
            else
                source.sendSuccess(EasyText.translatable("command.lightmanscurrency.lcbank.take.success", largestAmount.getComponent("NULL"), count), true);
        }
        return count;
    }

}
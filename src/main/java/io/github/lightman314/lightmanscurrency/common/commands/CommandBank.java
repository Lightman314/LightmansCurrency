package io.github.lightman314.lightmanscurrency.common.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.builtin.PlayerBankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.builtin.TeamBankAccountSource;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.MoneyValueArgument;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class CommandBank {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {

        LiteralArgumentBuilder<CommandSourceStack> bankCommand
                = Commands.literal("lcbank")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.literal("all")
                                .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                        .executes(c -> giveAll(c,true))
                                        .then(Commands.argument("notifyPlayers", BoolArgumentType.bool())
                                                .executes(c -> giveAll(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))
                        .then(Commands.literal("allPlayers")
                                .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                        .executes(c -> giveAllPlayers(c,true))
                                        .then(Commands.argument("notifyPlayers", BoolArgumentType.bool())
                                                .executes(c -> giveAllPlayers(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))
                        .then(Commands.literal("allTeams")
                                .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                        .executes(c -> giveAllTeams(c,true))
                                        .then(Commands.argument("notifyPlayers", BoolArgumentType.bool())
                                                .executes(c -> giveAllTeams(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))
                        .then(Commands.literal("players")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                                .executes(c -> givePlayers(c,true))
                                                .then(Commands.argument("notifyPlayers", BoolArgumentType.bool())
                                                        .executes(c -> givePlayers(c,BoolArgumentType.getBool(c,"notifyPlayers")))))))
                        .then(Commands.literal("team")
                                .then(Commands.argument("teamID", LongArgumentType.longArg(0))
                                        .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                                .executes(c -> giveTeam(c,true))
                                                .then(Commands.argument("notifyPlayers",BoolArgumentType.bool())
                                                        .executes(c -> giveTeam(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))))
                .then(Commands.literal("take")
                        .then(Commands.literal("all")
                                .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                        .executes(c -> takeAll(c, true))
                                        .then(Commands.argument("notifyPlayers",BoolArgumentType.bool())
                                                .executes(c -> takeAll(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))
                        .then(Commands.literal("allPlayers")
                                .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                        .executes(c -> takeAllPlayers(c,true))
                                        .then(Commands.argument("notifyPlayers",BoolArgumentType.bool())
                                                .executes(c -> takeAllPlayers(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))
                        .then(Commands.literal("allTeams")
                                .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                        .executes(c -> takeAllTeams(c,true))
                                        .then(Commands.argument("notifyPlayers",BoolArgumentType.bool())
                                                .executes(c -> takeAllTeams(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))
                        .then(Commands.literal("players")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                                .executes(c -> takePlayers(c,true))
                                                .then(Commands.argument("notifyPlayers",BoolArgumentType.bool())
                                                        .executes(c -> takePlayers(c,BoolArgumentType.getBool(c,"notifyPlayers")))))))
                        .then(Commands.literal("team")
                                .then(Commands.argument("teamID", LongArgumentType.longArg(0))
                                        .then(Commands.argument("amount", MoneyValueArgument.argument(context))
                                                .executes(c -> takeTeam(c,true))
                                                .then(Commands.argument("notifyPlayers",BoolArgumentType.bool())
                                                        .executes(c -> takeTeam(c,BoolArgumentType.getBool(c,"notifyPlayers"))))))))
                .then(Commands.literal("delete")
                        .then(Commands.literal("player")
                                .then(Commands.literal("online")
                                    .then(Commands.argument("player",EntityArgument.player())
                                            .executes(CommandBank::deletePlayerAccount)))
                                .then(Commands.literal("offline")
                                        .then(Commands.argument("nameOrID", StringArgumentType.word())
                                                .executes(CommandBank::deleteOfflinePlayerAccount)))));

        dispatcher.register(bankCommand);

    }

    static int giveAll(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws  CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), BankAPI.API.GetAllBankReferences(false), amount, notifyPlayers);
    }

    static int giveAllPlayers(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), PlayerBankAccountSource.INSTANCE.CollectAllReferences(false), amount, notifyPlayers);
    }

    static int giveAllTeams(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), TeamBankAccountSource.INSTANCE.CollectAllReferences(false), amount, notifyPlayers);
    }

    static int givePlayers(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        return giveTo(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "players").stream().map(PlayerBankReference::of).toList(), amount, notifyPlayers);
    }

    static int giveTeam(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        long teamID = LongArgumentType.getLong(commandContext, "teamID");
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        CommandSourceStack source = commandContext.getSource();
        Team team = TeamSaveData.GetTeam(false, teamID);
        if(team == null)
        {
            EasyText.sendCommandFail(source, LCText.COMMAND_BANK_TEAM_NULL.get(teamID));
            return 0;
        }
        else if(!team.hasBankAccount())
        {
            EasyText.sendCommandFail(source, LCText.COMMAND_BANK_TEAM_NO_BANK.get(teamID));
            return 0;
        }
        return giveTo(source, Lists.newArrayList(team.getBankReference()), amount, notifyPlayers);
    }

    static int giveTo(CommandSourceStack source, List<BankReference> accounts, MoneyValue amount, boolean notifyPlayers)
    {
        int count = 0;
        Component bankName = null;
        for(BankReference account : accounts)
        {
            if(BankAPI.API.BankDepositFromServer(account.get(),amount, notifyPlayers))
            {
                count++;
                if(count == 1)
                    bankName = account.get().getName();
            }

        }
        if(count < 1)
            EasyText.sendCommandFail(source, LCText.COMMAND_BANK_GIVE_FAIL.get());
        else
        {
            if(count == 1)
                EasyText.sendCommandSucess(source, LCText.COMMAND_BANK_GIVE_SUCCESS_SINGLE.get(amount.getText(), bankName), true);
            else
                EasyText.sendCommandSucess(source, LCText.COMMAND_BANK_GIVE_SUCCESS.get(amount.getText(), count), true);
        }
        return count;
    }

    static int takeAll(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), BankAPI.API.GetAllBankReferences(false), amount, notifyPlayers);
    }

    static int takeAllPlayers(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), PlayerBankAccountSource.INSTANCE.CollectAllReferences(false), amount, notifyPlayers);
    }

    static int takeAllTeams(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), TeamBankAccountSource.INSTANCE.CollectAllReferences(false), amount, notifyPlayers);
    }

    static int takePlayers(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        return takeFrom(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "players").stream().map(PlayerBankReference::of).toList(), amount, notifyPlayers);
    }

    static int takeTeam(CommandContext<CommandSourceStack> commandContext, boolean notifyPlayers) throws CommandSyntaxException
    {
        long teamID = LongArgumentType.getLong(commandContext, "teamID");
        MoneyValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        CommandSourceStack source = commandContext.getSource();
        Team team = TeamSaveData.GetTeam(false, teamID);
        if(team == null)
        {
            EasyText.sendCommandFail(source, LCText.COMMAND_BANK_TEAM_NULL.get(teamID));
            return 0;
        }
        else if(!team.hasBankAccount())
        {
            EasyText.sendCommandFail(source, LCText.COMMAND_BANK_TEAM_NO_BANK.get(teamID));
            return 0;
        }
        return takeFrom(commandContext.getSource(), Lists.newArrayList(team.getBankReference()), amount, notifyPlayers);
    }

    static int takeFrom(CommandSourceStack source, List<BankReference> accounts, MoneyValue amount, boolean notifyPlayers)
    {
        int count = 0;
        Component bankName = null;
        MoneyValue largestAmount = MoneyValue.empty();
        for(BankReference account : accounts)
        {
            Pair<Boolean, MoneyValue> result = BankAPI.API.BankWithdrawFromServer(account.get(), amount, notifyPlayers);
            if(result.getFirst())
            {
                count++;
                if(count == 1)
                    bankName = account.get().getName();
                if(result.getSecond().getCoreValue() > largestAmount.getCoreValue())
                    largestAmount = result.getSecond();
            }
        }
        if(count < 1)
            EasyText.sendCommandFail(source, LCText.COMMAND_BANK_TAKE_FAIL.get());
        else
        {
            if(count == 1)
                EasyText.sendCommandSucess(source, LCText.COMMAND_BANK_TAKE_SUCCESS_SINGLE.get(largestAmount.getText(), bankName), true);
            else
                EasyText.sendCommandSucess(source, LCText.COMMAND_BANK_TAKE_SUCCESS.get(largestAmount.getText(), count), true);
        }
        return count;
    }

    static int deletePlayerAccount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        Player player = EntityArgument.getPlayer(context,"player");

        BankSaveData.DeleteBankAccount(player.getUUID());
        EasyText.sendCommandSucess(context.getSource(),LCText.COMMAND_BANK_DELETE_PLAYER_RESET.get(player.getDisplayName()), true);
        return 1;
    }

    private static final DynamicCommandExceptionType INVALID_PLAYER_INPUT_TYPE = new DynamicCommandExceptionType(LCText.COMMAND_BANK_DELETE_PLAYER_INVALID_INPUT::get);

    static int deleteOfflinePlayerAccount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        String input = StringArgumentType.getString(context,"nameOrID");
        try {
            UUID playerID = UUID.fromString(input);
            PlayerReference pr = PlayerReference.of(playerID,"");
            return handleDeletion(pr,context.getSource());
        } catch (IllegalArgumentException e) {
            PlayerReference pr = PlayerReference.of(false,input);
            if(pr != null)
                return handleDeletion(pr,context.getSource());
            else
                throw INVALID_PLAYER_INPUT_TYPE.create(input);
        }
    }

    private static int handleDeletion(@Nonnull PlayerReference player, @Nonnull CommandSourceStack source)
    {
        if(BankSaveData.DeleteBankAccount(player.id))
        {
            EasyText.sendCommandSucess(source,LCText.COMMAND_BANK_DELETE_PLAYER_SUCCESS.get(player.getName(false)),true);
            return 1;
        }
        else
        {
            EasyText.sendCommandFail(source,LCText.COMMAND_BANK_DELETE_PLAYER_DOESNT_EXIST.get(player.getName(false)));
            return 0;
        }
    }

}

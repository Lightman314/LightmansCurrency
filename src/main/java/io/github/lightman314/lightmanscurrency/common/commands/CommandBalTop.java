package io.github.lightman314.lightmanscurrency.common.commands;

import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandBalTop {
	
	public static final int ENTRIES_PER_PAGE = 10;
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcAdminCommand
			= Commands.literal("lcbaltop")
				.executes(context -> CommandBalTop.execute(context, 1))
				.then(Commands.argument("page", IntegerArgumentType.integer(1))
					.executes(CommandBalTop::executePage));
		
		dispatcher.register(lcAdminCommand);
		
	}
	
	static int executePage(CommandContext<CommandSourceStack> commandContext) {
		
		return execute(commandContext, IntegerArgumentType.getInteger(commandContext, "page"));
		
	}
	
	static int execute(CommandContext<CommandSourceStack> commandContext, int page) {
		
		CommandSourceStack source = commandContext.getSource();
		
		//Get and sort all the bank accounts
		//Get player bank accounts
		List<BankReference> allAccounts = BankSaveData.GetPlayerBankAccounts();
		//Get team bank accounts
		List<Team> allTeams = TeamSaveData.GetAllTeams(false);
		for(Team team : allTeams) {
			if(team.hasBankAccount())
				allAccounts.add(team.getBankReference());
		}
		//Remove any accidental null entries from the list
		while(allAccounts.remove(null));
		//Sort the bank account by balance (and name if balance is tied).
		allAccounts.sort(new AccountSorter());
		
		
		int startIndex = (page - 1) * ENTRIES_PER_PAGE;
		
		if(startIndex >= allAccounts.size())
		{
			EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbaltop.error.page"));
			return 0;
		}


		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcbaltop.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), false);
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcbaltop.page", page, getMaxPage(allAccounts.size())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), false);
		for(int i = startIndex; i < startIndex + ENTRIES_PER_PAGE && i < allAccounts.size(); ++i)
		{
			try {
				BankAccount account = allAccounts.get(i).get();
				Component name = account.getName();
				String amount = account.getCoinStorage().getString("0");
				EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcbaltop.entry", i + 1, name, amount), false);
			} catch(Exception ignored) { }
		}
		
		return 1;
	}
	
	private static int getMaxPage(int listSize) {
		return ((listSize - 1) / ENTRIES_PER_PAGE) + 1;
	}
	
	private static class AccountSorter implements Comparator<BankReference> {

		@Override
		public int compare(BankReference o1, BankReference o2) {
			BankAccount a1 = o1 == null ? null : o1.get();
			BankAccount a2 = o2 == null ? null : o2.get();
			if(o1 == o2)
				return 0;
			if(o1 == null)
				return 1;
			if(o2 == null)
				return -1;
			long bal1 = a1.getCoinStorage().getValueNumber();
			long bal2 = a2.getCoinStorage().getValueNumber();
			
			if(bal1 > bal2)
				return -1;
			if(bal2 > bal1)
				return 1;
			
			//Sort by name
			return a1.getName().getString().toLowerCase().compareTo(a2.getName().getString().toLowerCase());
		}
		
	}
	
	
	
}

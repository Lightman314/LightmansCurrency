package io.github.lightman314.lightmanscurrency.commands;

import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

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
	
	static int executePage(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		
		return execute(commandContext, IntegerArgumentType.getInteger(commandContext, "page"));
		
	}
	
	static int execute(CommandContext<CommandSourceStack> commandContext, int page) throws CommandSyntaxException {
		
		CommandSourceStack source = commandContext.getSource();
		
		//Get and sort all of the bank accounts
		//Get player bank accounts
		List<AccountReference> allAccounts = TradingOffice.getPlayerBankAccounts();
		//Get team bank accounts
		List<Team> allTeams = TradingOffice.getTeams();
		for(Team team : allTeams) {
			if(team.hasBankAccount())
				allAccounts.add(BankAccount.GenerateReference(false, team));
		}
		//Remove any accidental null entries from the list
		while(allAccounts.remove(null));
		//Sort the bank account by balance (and name if balance is tied).
		allAccounts.sort(new AccountSorter());
		
		
		int startIndex = (page - 1) * ENTRIES_PER_PAGE;
		
		if(startIndex >= allAccounts.size())
		{
			source.sendFailure(new TranslatableComponent("command.lightmanscurrency.lcbaltop.error.page"));
			return 0;
		}
			
		
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcbaltop.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), false);
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcbaltop.page", page, getMaxPage(allAccounts.size())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), false);
		for(int i = startIndex; i < startIndex + ENTRIES_PER_PAGE && i < allAccounts.size(); ++i)
		{
			try {
				BankAccount account = allAccounts.get(i).get();
				Component name = account.getName();
				String amount = account.getCoinStorage().getString("0");
				source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcbaltop.entry", i + 1, name, amount), false);
			} catch(Exception e) { }
		}
		
		return 1;
	}
	
	private static int getMaxPage(int listSize) {
		return ((listSize - 1) / ENTRIES_PER_PAGE) + 1;
	}
	
	private static class AccountSorter implements Comparator<AccountReference> {

		@Override
		public int compare(AccountReference o1, AccountReference o2) {
			BankAccount a1 = o1 == null ? null : o1.get();
			BankAccount a2 = o2 == null ? null : o2.get();
			if(o1 == o2)
				return 0;
			if(o1 == null)
				return 1;
			if(o2 == null)
				return -1;
			long bal1 = a1.getCoinStorage().getRawValue();
			long bal2 = a2.getCoinStorage().getRawValue();
			
			if(bal1 > bal2)
				return -1;
			if(bal2 > bal1)
				return 1;
			
			//Sort by name
			return a1.getName().getString().toLowerCase().compareTo(a2.getName().getString().toLowerCase());
		}
		
	}
	
	
	
}

package io.github.lightman314.lightmanscurrency.common.commands;

import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandBalTop {

	public static final Comparator<IBankAccount> SORTER = new AccountSorter();

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
		//Get bank accounts
		List<IBankAccount> allAccounts = BankAPI.getApi().GetAllBankAccounts(false);
		//Remove any accidental null entries from the list
		allAccounts.removeIf(ba -> {
			if(ba == null)
				return true;
			return ba.getMoneyStorage().isEmpty();
		});
		if(allAccounts.isEmpty())
		{
			EasyText.sendCommandFail(source, LCText.COMMAND_BALTOP_NO_RESULTS.get());
			return 0;
		}

		allAccounts.sort(SORTER);
		
		int startIndex = (page - 1) * ENTRIES_PER_PAGE;
		
		if(startIndex >= allAccounts.size())
		{
			EasyText.sendCommandFail(source, LCText.COMMAND_BALTOP_ERROR_PAGE.get());
			return 0;
		}


		EasyText.sendCommandSucess(source, LCText.COMMAND_BALTOP_TITLE.get().withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), false);
		EasyText.sendCommandSucess(source, LCText.COMMAND_BALTOP_PAGE.get(page, getMaxPage(allAccounts.size())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), false);
		for(int i = startIndex; i < startIndex + ENTRIES_PER_PAGE && i < allAccounts.size(); ++i)
		{
			try {
				IBankAccount account = allAccounts.get(i);
				Component name = account.getName();
				Component amount = account.getMoneyStorage().getAllValueText();
				EasyText.sendCommandSucess(source, LCText.COMMAND_BALTOP_ENTRY.get(i + 1, name, amount), false);
			} catch(Exception ignored) { }
		}
		
		return 1;
	}
	
	private static int getMaxPage(int listSize) {
		return ((listSize - 1) / ENTRIES_PER_PAGE) + 1;
	}

	private static class AccountSorter implements Comparator<IBankAccount> {

		@Override
		public int compare(IBankAccount a1, IBankAccount a2) {
			long bal1 = 0;
			for(MoneyValue val : a1.getMoneyStorage().allValues())
				bal1 += val.getCoreValue();
			long bal2 = 0;
			for(MoneyValue val : a2.getMoneyStorage().allValues())
				bal2 += val.getCoreValue();
			
			if(bal1 > bal2)
				return -1;
			if(bal2 > bal1)
				return 1;
			
			//Sort by name
			return a1.getName().getString().toLowerCase().compareTo(a2.getName().getString().toLowerCase());
		}
		
	}
	
	
	
}

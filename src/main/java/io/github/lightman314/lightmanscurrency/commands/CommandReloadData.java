package io.github.lightman314.lightmanscurrency.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandReloadData {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcReloadCommand
			= Commands.literal("lcreload")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.executes(CommandReloadData::execute);
		
		dispatcher.register(lcReloadCommand);
		
	}
	
	static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		
		TraderSaveData.ReloadPersistentTraders();
		MoneyUtil.reloadMoneyData();
		ATMData.reloadATMData();
		commandContext.getSource().sendSuccess(Component.translatable("command.lightmanscurrency.lcreload"), true);
		return 1;
		
	}
	
}

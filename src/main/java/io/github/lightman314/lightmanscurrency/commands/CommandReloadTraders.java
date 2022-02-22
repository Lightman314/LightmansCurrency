package io.github.lightman314.lightmanscurrency.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class CommandReloadTraders {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcReloadCommand
			= Commands.literal("lcreload")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.executes(CommandReloadTraders::execute);
		
		dispatcher.register(lcReloadCommand);
		
	}
	
	static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		TradingOffice.reloadPersistentTraders();
		commandContext.getSource().sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcreload"), true);
		return 1;
		
	}
	
}

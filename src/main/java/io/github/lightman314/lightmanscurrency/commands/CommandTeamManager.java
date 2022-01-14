package io.github.lightman314.lightmanscurrency.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageOpenTeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

public class CommandTeamManager {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcTeamManagerCommand
			= Commands.literal("lcteammanager")
				.requires((commandSource) -> commandSource.getEntity() instanceof Player)
				.executes(CommandTeamManager::execute);
		
		dispatcher.register(lcTeamManagerCommand);
		
	}
	
	static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		PacketTarget target = LightmansCurrencyPacketHandler.getTarget(commandContext.getSource().getPlayerOrException());
		LightmansCurrencyPacketHandler.instance.send(target, new MessageOpenTeamManager());
		return 1;
		
	}
	
}

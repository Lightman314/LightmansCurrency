package io.github.lightman314.lightmanscurrency.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageOpenTeamManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class CommandTeamManager {
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> lcTeamManagerCommand
			= Commands.literal("lcteammanager")
				.requires((commandSource) -> commandSource.getEntity() instanceof PlayerEntity)
				.executes(CommandTeamManager::execute);
		
		dispatcher.register(lcTeamManagerCommand);
		
	}
	
	static int execute(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		PacketTarget target = LightmansCurrencyPacketHandler.getTarget(commandContext.getSource().asPlayer());
		LightmansCurrencyPacketHandler.instance.send(target, new MessageOpenTeamManager());
		return 1;
		
	}
	
}

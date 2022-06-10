package io.github.lightman314.lightmanscurrency.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class CommandLCAdmin {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcAdminCommand
			= Commands.literal("lcadmin")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.then(Commands.literal("help")
						.executes(CommandLCAdmin::help))
				.then(Commands.literal("toggleadmin")
						.requires((commandSource) -> commandSource.getEntity() instanceof ServerPlayer)
						.executes(CommandLCAdmin::toggleAdmin))
				.then(Commands.literal("universaldata")
						.then(Commands.literal("list")
							.executes(CommandLCAdmin::listUniversalData))
						.then(Commands.literal("search")
								.then(Commands.argument("searchText", MessageArgument.message())
										.executes(CommandLCAdmin::searchUniversalData)))
						.then(Commands.literal("delete")
								.then(Commands.argument("dataID", MessageArgument.message())
										.executes(CommandLCAdmin::deleteUniversalData))));
		
		dispatcher.register(lcAdminCommand);
		
	}
	
	static int help(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		
		//help
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.help.help", "/lcadmin help -> "), false);
		//toggleadmin
		if(source.getEntity() instanceof ServerPlayer)
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin.help", "/lcadmin toggleadmin -> "), false);
		//universaldata list
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.help", "/lcadmin universaldata list -> "), false);
		//universaldata search
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.help", "/lcadmin universaldata search <searchText> -> "), false);
		//universaldata delete <traderID>
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.help", "/lcadmin universaldata delete <dataID> "), false);
		
		return 1;
	}
	
	static int toggleAdmin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer sourcePlayer = source.getPlayerOrException();
		
		TradingOffice.toggleAdminPlayer(sourcePlayer);
		Component enabledDisabled = TradingOffice.isAdminPlayer(sourcePlayer) ? Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin.enabled").withStyle(ChatFormatting.GREEN) : Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin.disabled").withStyle(ChatFormatting.RED);
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin", enabledDisabled), true);
		
		return 1;
	}
	
	static int listUniversalData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		List<UniversalTraderData> allTraders = TradingOffice.getTraders();
		
		if(allTraders.size() > 0)
		{
			
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			
			for(int i = 0; i < allTraders.size(); i++)
			{
				UniversalTraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					source.sendSuccess(Component.empty(), true);
				
				sendTraderDataFeedback(thisTrader, source);
				
			}
		}
		else
		{
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.none"), true);
		}
		
		return 1;
	}
	
	static int searchUniversalData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		
		String searchText = MessageArgument.getMessage(commandContext, "searchText").getString();
		
		List<UniversalTraderData> allTraders = TradingOffice.getTraders(searchText);
		if(allTraders.size() > 0)
		{
			
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			for(int i = 0; i < allTraders.size(); i++)
			{
				UniversalTraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					source.sendSuccess(Component.empty(), true);
				
				sendTraderDataFeedback(thisTrader, source);
				
			}
		}
		else
		{
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.search.none"), true);
		}
		
		return 1;
	}
	
	private static void sendTraderDataFeedback(UniversalTraderData thisTrader, CommandSourceStack source)
	{
		//Trader ID
		String traderID = thisTrader.getTraderID().toString();
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid", Component.literal(traderID).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, traderID)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid.copytooltip"))))), true);
		//Type
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.type", thisTrader.getTraderType()), true);
		
		//Team / Team ID
		if(thisTrader.getCoreSettings().getTeam() != null)
		{
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.team", thisTrader.getCoreSettings().getTeam().getName(), thisTrader.getCoreSettings().getTeam().getID().toString()), true);
		}
		//Owner / Owner ID
		else if(thisTrader.getCoreSettings().getOwner() != null)
		{
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner", thisTrader.getCoreSettings().getOwner().lastKnownName(), thisTrader.getCoreSettings().getOwner().id.toString()), true);
		}
		else
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.none"), true);
		
		//Dimension
		String dimension = thisTrader.getWorld().location().toString();
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.dimension", dimension), true);
		//Position
		BlockPos pos = thisTrader.getPos();
		String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
		String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position", Component.literal(position).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position.teleporttooltip"))))), true);
		//Custom Name (if applicable)
		if(thisTrader.getCoreSettings().hasCustomName())
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.name", thisTrader.getName()), true);
	}
	
	static int deleteUniversalData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		
		CommandSourceStack source = commandContext.getSource();
		
		String traderID = MessageArgument.getMessage(commandContext, "dataID").getString();
		if(traderID == "")
		{
			source.sendFailure(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.noid"));
			return 0;
		}
		List<UniversalTraderData> allTraders = TradingOffice.getTraders();
		for(int i = 0; i < allTraders.size(); i++)
		{
			if(allTraders.get(i).getTraderID().toString().equals(traderID))
			{
				//Remove the trader
				TradingOffice.removeTrader(allTraders.get(i).getTraderID());
				//Send success message
				source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.success", traderID), true);
				return 1;
			}
		}
		//If no trader with that id found, send a not found message
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.notfound"), true);
		return 0;
	}
	
}

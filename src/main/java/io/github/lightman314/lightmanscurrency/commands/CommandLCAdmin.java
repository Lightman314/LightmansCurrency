package io.github.lightman314.lightmanscurrency.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandLCAdmin {
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> lcAdminCommand
			= Commands.literal("lcadmin")
				.requires((commandSource) -> commandSource.hasPermissionLevel(2))
				.then(Commands.literal("help")
						.executes(CommandLCAdmin::help))
				.then(Commands.literal("toggleadmin")
						.requires((commandSource) -> commandSource.getEntity() instanceof ServerPlayerEntity)
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
	
	static int help(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		CommandSource source = commandContext.getSource();
		
		//help
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.help.help", "/lcadmin help -> "), false);
		//toggleadmin
		if(source.getEntity() instanceof ServerPlayerEntity)
			source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.toggleadmin.help", "/lcadmin toggleadmin -> "), false);
		//universaldata list
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.help", "/lcadmin universaldata list -> "), false);
		//universaldata delete <traderID>
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.delete.help", "/lcadmin universaldata delete <traderID> "), false);
		
		return 1;
	}
	
	static int toggleAdmin(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		CommandSource source = commandContext.getSource();
		ServerPlayerEntity sourcePlayer = source.asPlayer();
		
		TradingOffice.toggleAdminPlayer(sourcePlayer);
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.toggleadmin",new TranslationTextComponent("command.lightmanscurrency.lcadmin.toggleadmin." + (TradingOffice.isAdminPlayer(sourcePlayer) ? "enabled" : "disabled"))), true);
		
		return 1;
	}
	
	static int listUniversalData(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		CommandSource source = commandContext.getSource();
		List<UniversalTraderData> allTraders = TradingOffice.getTraders();
		
		if(allTraders.size() > 0)
		{
			
			source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			
			for(int i = 0; i < allTraders.size(); i++)
			{
				UniversalTraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					source.sendFeedback(new StringTextComponent(""), true);
				
				sendTraderDataFeedback(thisTrader, source);
				
			}
		}
		else
		{
			source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.none"), true);
		}
		
		return 1;
	}
	
	static int searchUniversalData(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		CommandSource source = commandContext.getSource();
		
		String searchText = MessageArgument.getMessage(commandContext, "searchText").getString();
		
		List<UniversalTraderData> allTraders = TradingOffice.getTraders(searchText);
		if(allTraders.size() > 0)
		{
			
			source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			for(int i = 0; i < allTraders.size(); i++)
			{
				UniversalTraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					source.sendFeedback(new StringTextComponent(""), true);
				
				sendTraderDataFeedback(thisTrader, source);
				
			}
		}
		else
		{
			source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.search.none"), true);
		}
		
		return 1;
	}
	
	private static void sendTraderDataFeedback(UniversalTraderData thisTrader, CommandSource source)
	{
		//Trader ID
		String traderID = thisTrader.getTraderID().toString();
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.traderid", new StringTextComponent(traderID).mergeStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, traderID)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.traderid.copytooltip"))))), true);
		//Type
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.type", thisTrader.getDeserializerType()), true);
		//Owner / Owner ID
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.owner", thisTrader.getOwnerName(), thisTrader.getOwnerID().toString()), true);
		//Dimension
		String dimension = thisTrader.getWorld().getLocation().toString();
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.dimension", dimension), true);
		//Position
		BlockPos pos = thisTrader.getPos();
		String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
		String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.position", new StringTextComponent(position).mergeStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.position.teleporttooltip"))))), true);
		//Custom Name (if applicable)
		if(thisTrader.hasCustomName())
			source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.list.name", thisTrader.getName()), true);
	}
	
	static int deleteUniversalData(CommandContext<CommandSource> commandContext) throws CommandSyntaxException
	{
		
		CommandSource source = commandContext.getSource();
		
		String traderID = MessageArgument.getMessage(commandContext, "dataID").getString();
		if(traderID == "")
		{
			source.sendErrorMessage(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.delete.noid"));
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
				source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.delete.success", traderID), true);
				return 1;
			}
		}
		//If no trader with that id found, send a not found message
		source.sendFeedback(new TranslationTextComponent("command.lightmanscurrency.lcadmin.universaldata.delete.notfound"), true);
		return 0;
	}
	
}

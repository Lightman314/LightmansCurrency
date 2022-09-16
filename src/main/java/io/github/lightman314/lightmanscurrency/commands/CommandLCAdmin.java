package io.github.lightman314.lightmanscurrency.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.commands.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerWhitelist;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public class CommandLCAdmin {
	
	
	private static List<UUID> adminPlayers = new ArrayList<>();
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcAdminCommand
			= Commands.literal("lcadmin")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.then(Commands.literal("toggleadmin")
						.requires((commandSource) -> commandSource.getEntity() instanceof ServerPlayer)
						.executes(CommandLCAdmin::toggleAdmin))
				.then(Commands.literal("setCustomTrader")
						.then(Commands.argument("traderPos", BlockPosArgument.blockPos())
								.executes(CommandLCAdmin::setCustomTrader)))
				.then(Commands.literal("traderdata")
						.then(Commands.literal("list")
							.executes(CommandLCAdmin::listTraderData))
						.then(Commands.literal("search")
								.then(Commands.argument("searchText", MessageArgument.message())
										.executes(CommandLCAdmin::searchTraderData)))
						.then(Commands.literal("delete")
								.then(Commands.argument("traderID", TraderArgument.trader())
										.executes(CommandLCAdmin::deleteTraderData)))
						.then(Commands.literal("debug")
								.then(Commands.argument("traderID", TraderArgument.trader())
										.executes(CommandLCAdmin::debugTraderData)))
						.then(Commands.literal("addToWhitelist")
								.then(Commands.argument("traderID", TraderArgument.traderWithPersistent())
										.then(Commands.argument("player", EntityArgument.players())
												.executes(CommandLCAdmin::addToTraderWhitelist)))));
		
		dispatcher.register(lcAdminCommand);
		
	}
	
	static int toggleAdmin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer sourcePlayer = source.getPlayerOrException();
		
		ToggleAdminPlayer(sourcePlayer);
		Component enabledDisabled = isAdminPlayer(sourcePlayer) ? Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin.enabled").withStyle(ChatFormatting.GREEN) : Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin.disabled").withStyle(ChatFormatting.RED);
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.toggleadmin", enabledDisabled), true);
		
		return 1;
	}
	
	private static final SimpleCommandExceptionType ERROR_BLOCK_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("command.trader.block.notfound"));
	
	static int setCustomTrader(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		
		CommandSourceStack source = commandContext.getSource();
		
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(commandContext, "traderPos");
		
		Level level = source.getLevel();
		
		BlockState state = level.getBlockState(pos);
		BlockEntity be = null;
		if(state.getBlock() instanceof ITraderBlock)
			be = ((ITraderBlock)state.getBlock()).getBlockEntity(state, level, pos);
		else
			be = level.getBlockEntity(pos);
		
		if(be instanceof TraderBlockEntity<?>)
		{
			TraderBlockEntity<?> t = (TraderBlockEntity<?>)be;
			t.saveCurrentTraderAsCustomTrader();
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.setCustomTrader.success"), true);
			return 1;
		}
		
		throw ERROR_BLOCK_NOT_FOUND.create();
		
	}
	
	static int listTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		List<TraderData> allTraders = TraderSaveData.GetAllTraders(false);
		
		if(allTraders.size() > 0)
		{
			
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			
			for(int i = 0; i < allTraders.size(); i++)
			{
				TraderData thisTrader = allTraders.get(i);
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
	
	static int searchTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		
		String searchText = MessageArgument.getMessage(commandContext, "searchText").getString();
		
		List<TraderData> results = TraderSaveData.GetAllTraders(false).stream().filter(trader -> TraderSearchFilter.CheckFilters(trader, searchText)).collect(Collectors.toList());
		if(results.size() > 0)
		{
			
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			for(int i = 0; i < results.size(); i++)
			{
				TraderData thisTrader = results.get(i);
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
	
	private static void sendTraderDataFeedback(TraderData thisTrader, CommandSourceStack source)
	{
		//Trader ID
		String traderID = String.valueOf(thisTrader.getID());
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid", Component.literal(traderID).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, traderID)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid.copytooltip"))))), false);
		//Persistent ID
		if(thisTrader.isPersistent())
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.persistentid", thisTrader.getPersistentID()), false);
		
		//Type
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.type", thisTrader.type), false);
		
		//Ignore everything else for auction houses
		if(thisTrader instanceof AuctionHouseTrader)
			return;
		
		//Team / Team ID
		if(thisTrader.getOwner().hasTeam())
		{
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.team", thisTrader.getOwner().getTeam().getName(), thisTrader.getOwner().getTeam().getID()), false);
		}
		//Owner / Owner ID
		else if(thisTrader.getOwner().hasPlayer())
		{
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner", thisTrader.getOwner().getPlayer().lastKnownName(), thisTrader.getOwner().getPlayer().id.toString()), false);
		}
		else
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.custom", thisTrader.getOwner().getOwnerName()), false);
		
		if(!thisTrader.isPersistent())
		{
			//Dimension
			String dimension = thisTrader.getLevel().location().toString();
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.dimension", dimension), false);
			//Position
			BlockPos pos = thisTrader.getPos();
			String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
			String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position", Component.literal(position).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position.teleporttooltip"))))), true);
		}
		//Custom Name (if applicable)
		if(thisTrader.hasCustomName())
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.list.name", thisTrader.getName()), true);
	}
	
	static int deleteTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		
		CommandSourceStack source = commandContext.getSource();
		
		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");
		
		//Remove the trader
		TraderSaveData.DeleteTrader(trader.getID());
		//Send success message
		source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.success", trader.getName()), true);
		return 1;
		
	}
	
	static int debugTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		CommandSourceStack source = commandContext.getSource();
		
		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");
		source.sendSuccess(Component.literal(trader.save().getAsString()), false);
		return 1;
	}
	
	static int addToTraderWhitelist(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		
		CommandSourceStack source = commandContext.getSource();
		
		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");
		
		TradeRule rule = TradeRule.getRule(PlayerWhitelist.TYPE, trader.getRules());
		if(rule instanceof PlayerWhitelist)
		{
			PlayerWhitelist whitelist = (PlayerWhitelist)rule;
			Collection<ServerPlayer> players = EntityArgument.getPlayers(commandContext, "player");
			int count = 0;
			for(ServerPlayer player : players)
			{
				if(whitelist.addToWhitelist(player))
					count++;
			}
			source.sendSuccess(Component.translatable("command.lightmanscurrency.lcadmin.traderdata.add_whitelist.success", count, trader.getName()), true);
			
			if(count > 0)
				trader.markRulesDirty();
			
			return count;
		}
		else
		{
			source.sendFailure(Component.translatable("command.lightmanscurrency.lcadmin.traderdata.add_whitelist.missingrule"));
			return 0;
		}
		
	}
	
	
	
	public static boolean isAdminPlayer(Player player) { return adminPlayers.contains(player.getUUID()) && player.hasPermissions(2); }
	
	
	private static void ToggleAdminPlayer(ServerPlayer player) {
		UUID playerID = player.getUUID();
		if(adminPlayers.contains(playerID))
		{
			adminPlayers.remove(playerID);
			if(!player.level.isClientSide)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
		else
		{
			adminPlayers.add(playerID);
			if(!player.level.isClientSide)
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), getAdminSyncMessage());
			}
		}
	}
	
	private static MessageSyncAdminList getAdminSyncMessage() { return new MessageSyncAdminList(adminPlayers); }
	
	public static void loadAdminPlayers(List<UUID> serverAdminList) { adminPlayers = serverAdminList; }
	
}

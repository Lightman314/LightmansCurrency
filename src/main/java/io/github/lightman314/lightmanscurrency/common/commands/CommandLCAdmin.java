package io.github.lightman314.lightmanscurrency.common.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerWhitelist;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageDebugTrader;
import io.github.lightman314.lightmanscurrency.network.message.command.MessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.secrets.Secret;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class CommandLCAdmin {


	private static List<UUID> adminPlayers = new ArrayList<>();

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcAdminCommand
				= Commands.literal("lcadmin")
				.requires((stack) -> stack.hasPermission(2) || Secret.hasSecretAccess(stack))
				.then(Commands.literal("toggleadmin")
						.requires(stack -> stack.getEntity() instanceof ServerPlayer)
						.executes(CommandLCAdmin::toggleAdmin))
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
												.executes(CommandLCAdmin::addToTraderWhitelist)))))
				.then(Commands.literal("prepareForStructure")
						.then(Commands.argument("traderPos", BlockPosArgument.blockPos())
								.executes(CommandLCAdmin::setCustomTrader)))
				.then(Commands.literal("replaceWallet").requires((c) -> !LightmansCurrency.isCuriosLoaded())
						.then(Commands.argument("entity", EntityArgument.entities())
								.then(Commands.argument("wallet", ItemArgument.item())
										.executes(CommandLCAdmin::replaceWalletSlotWithDefault)
										.then(Commands.argument("keepWalletContents", BoolArgumentType.bool())
												.executes(CommandLCAdmin::replaceWalletSlot)))))
				.then(Commands.literal("taxes")
						.then(Commands.literal("openServerTax")
								.requires(stack -> stack.getEntity() instanceof ServerPlayer)
								.executes(CommandLCAdmin::openServerTax))
						.then(Commands.literal("forceDisableTaxCollectors")
								.executes(CommandLCAdmin::forceDisableTaxCollectors)));

		dispatcher.register(lcAdminCommand);

	}

	static int toggleAdmin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{

		CommandSourceStack source = commandContext.getSource();
		ServerPlayer sourcePlayer = source.getPlayerOrException();

		ToggleAdminPlayer(sourcePlayer);
		Component enabledDisabled = isAdminPlayer(sourcePlayer) ? EasyText.translatable("command.lightmanscurrency.lcadmin.toggleadmin.enabled").withStyle(ChatFormatting.GREEN) : EasyText.translatable("command.lightmanscurrency.lcadmin.toggleadmin.disabled").withStyle(ChatFormatting.RED);
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.toggleadmin", enabledDisabled), true);

		return 1;
	}

	private static final SimpleCommandExceptionType ERROR_BLOCK_NOT_FOUND = new SimpleCommandExceptionType(EasyText.translatable("command.trader.block.notfound"));

	static int setCustomTrader(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

		CommandSourceStack source = commandContext.getSource();

		BlockPos pos = BlockPosArgument.getLoadedBlockPos(commandContext, "traderPos");

		Level level = source.getLevel();

		BlockState state = level.getBlockState(pos);
		BlockEntity be;
		if(state.getBlock() instanceof ITraderBlock)
			be = ((ITraderBlock)state.getBlock()).getBlockEntity(state, level, pos);
		else
			be = level.getBlockEntity(pos);

		if(be instanceof TraderBlockEntity<?> t)
		{
			t.saveCurrentTraderAsCustomTrader();
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.setCustomTrader.success"), true);
			return 1;
		}

		throw ERROR_BLOCK_NOT_FOUND.create();

	}

	static int listTraderData(CommandContext<CommandSourceStack> commandContext) {

		CommandSourceStack source = commandContext.getSource();
		List<TraderData> allTraders = TraderSaveData.GetAllTraders(false);

		if(allTraders.size() > 0)
		{

			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);

			for(int i = 0; i < allTraders.size(); i++)
			{
				TraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					EasyText.sendCommandSucess(source, EasyText.empty(), true);

				sendTraderDataFeedback(thisTrader, source);
			}
		}
		else
		{
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.none"), true);
		}

		return 1;
	}

	static int searchTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{

		CommandSourceStack source = commandContext.getSource();

		String searchText = MessageArgument.getMessage(commandContext, "searchText").getString();

		List<TraderData> results = TraderSaveData.GetAllTraders(false).stream().filter(trader -> TraderSearchFilter.CheckFilters(trader, searchText)).toList();
		if(results.size() > 0)
		{

			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			for(int i = 0; i < results.size(); i++)
			{
				TraderData thisTrader = results.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					EasyText.sendCommandSucess(source, EasyText.empty(), true);

				sendTraderDataFeedback(thisTrader, source);
			}
		}
		else
		{
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.search.none"), true);
		}

		return 1;
	}

	private static void sendTraderDataFeedback(TraderData thisTrader, CommandSourceStack source)
	{
		//Trader ID
		String traderID = String.valueOf(thisTrader.getID());
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid", EasyText.translatable(traderID).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, traderID)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid.copytooltip"))))), false);
		//Persistent ID
		if(thisTrader.isPersistent())
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.persistentid", thisTrader.getPersistentID()), false);

		//Type
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.type", thisTrader.type), false);

		//Ignore everything else for auction houses
		if(thisTrader instanceof AuctionHouseTrader)
			return;

		//Team / Team ID
		if(thisTrader.getOwner().hasTeam())
		{
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.team", thisTrader.getOwner().getTeam().getName(), thisTrader.getOwner().getTeam().getID()), false);
		}
		//Owner / Owner ID
		else if(thisTrader.getOwner().hasPlayer())
		{
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner", thisTrader.getOwner().getPlayer().getName(false), thisTrader.getOwner().getPlayer().id.toString()), false);
		}
		else
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.custom", thisTrader.getOwner().getOwnerName(false)), false);

		if(!thisTrader.isPersistent())
		{
			//Dimension
			String dimension = thisTrader.getLevel().location().toString();
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.dimension", dimension), false);
			//Position
			BlockPos pos = thisTrader.getPos();
			String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
			String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position", EasyText.literal(position).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position.teleporttooltip"))))), true);
		}
		//Custom Name (if applicable)
		if(thisTrader.hasCustomName())
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.list.name", thisTrader.getName()), true);
	}

	static int deleteTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{

		CommandSourceStack source = commandContext.getSource();

		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");

		//Remove the trader
		TraderSaveData.DeleteTrader(trader.getID());
		//Send success message
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.success", trader.getName()), true);
		if(source.getEntity() != null && source.getEntity() instanceof Player)
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget((Player)source.getEntity()), new MessageDebugTrader(trader.getID()));
		return 1;

	}

	static int debugTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		CommandSourceStack source = commandContext.getSource();

		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");
		EasyText.sendCommandSucess(source, EasyText.literal(trader.save().getAsString()), false);
		return 1;
	}

	static int addToTraderWhitelist(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{

		CommandSourceStack source = commandContext.getSource();

		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");

		TradeRule rule = TradeRule.getRule(PlayerWhitelist.TYPE, trader.getRules());
		if(rule instanceof PlayerWhitelist whitelist)
		{
			Collection<ServerPlayer> players = EntityArgument.getPlayers(commandContext, "player");
			int count = 0;
			for(ServerPlayer player : players)
			{
				if(whitelist.addToWhitelist(player))
					count++;
			}
			final int c = count;
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.traderdata.add_whitelist.success", c, trader.getName()), true);

			if(count > 0)
				trader.markTradeRulesDirty();

			return count;
		}
		else
		{
			EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcadmin.traderdata.add_whitelist.missingrule"));
			return 0;
		}

	}

	static int replaceWalletSlotWithDefault(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		return replaceWalletSlotInternal(commandContext, true);
	}

	static int replaceWalletSlot(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		return replaceWalletSlotInternal(commandContext, BoolArgumentType.getBool(commandContext, "keepWalletContents"));
	}

	static int replaceWalletSlotInternal(CommandContext<CommandSourceStack> commandContext, boolean keepWalletContents) throws CommandSyntaxException {
		int count = 0;
		ItemInput input = ItemArgument.getItem(commandContext,"wallet");
		if(!(input.getItem() instanceof WalletItem) && input.getItem() != Items.AIR)
			throw new CommandSyntaxException(new CommandExceptionType() {}, EasyText.translatable("command.lightmanscurrency.lcadmin.replaceWalletSlot.notawallet"));
		for(Entity entity : EntityArgument.getEntities(commandContext,"entity"))
		{
			IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
			if(walletHandler != null)
			{
				ItemStack newWallet = input.createItemStack(1, true);
				if(newWallet.isEmpty() && walletHandler.getWallet().isEmpty())
					continue;
				if(keepWalletContents)
				{
					ItemStack oldWallet = walletHandler.getWallet();
					if(WalletItem.isWallet(oldWallet))
						WalletItem.CopyWalletContents(oldWallet, newWallet);
				}
				walletHandler.setWallet(newWallet);
			}
		}
		return count;
	}

	static int openServerTax(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		TaxEntry entry = TaxSaveData.GetServerTaxEntry(false);
		if(entry != null)
		{
			entry.openMenu(commandContext.getSource().getPlayerOrException(), SimpleValidator.NULL);
			return 1;
		}
		else
			EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.openServerTax.error"));
		return 0;
	}

	static int forceDisableTaxCollectors(CommandContext<CommandSourceStack> commandContext)
	{
		int count = 0;
		for(TaxEntry entry : TaxSaveData.GetAllTaxEntries(false))
		{
			if(entry.isActive())
			{
				entry.setActive(false, null);
				count++;
			}
		}
		if(count > 0)
			EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.forceDisableTaxCollectors.success", count), true);
		else
			EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.forceDisableTaxCollectors.fail"));
		return count;
	}

	public static boolean isAdminPlayer(@Nullable Player player) { return player != null && adminPlayers.contains(player.getUUID()) && (player.hasPermissions(2) || Secret.hasSecretAccess(player)); }


	private static void ToggleAdminPlayer(ServerPlayer player) {
		UUID playerID = player.getUUID();
		if(adminPlayers.contains(playerID))
			adminPlayers.remove(playerID);
		else
			adminPlayers.add(playerID);
		LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageSyncAdminList(adminPlayers));
	}

	public static MessageSyncAdminList getAdminSyncMessage() { return new MessageSyncAdminList(adminPlayers); }

	public static void loadAdminPlayers(List<UUID> serverAdminList) { adminPlayers = serverAdminList; }

}
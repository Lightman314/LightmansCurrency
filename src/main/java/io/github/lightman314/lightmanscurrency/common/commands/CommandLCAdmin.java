package io.github.lightman314.lightmanscurrency.common.commands;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.capability.event_unlocks.CapabilityEventUnlocks;
import io.github.lightman314.lightmanscurrency.common.capability.event_unlocks.IEventUnlocks;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxSaveData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerWhitelist;
import io.github.lightman314.lightmanscurrency.network.message.command.SPacketDebugTrader;
import io.github.lightman314.lightmanscurrency.secrets.Secret;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
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

public class CommandLCAdmin {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcAdminCommand
				= Commands.literal("lcadmin")
				.requires((stack) -> stack.hasPermission(2) || Secret.hasSecretAccess(stack))
				.then(Commands.literal("toggleadmin")
						.requires(CommandSourceStack::isPlayer)
						.executes(CommandLCAdmin::toggleAdmin))
				.then(Commands.literal("traderdata")
						.then(Commands.literal("list")
								.executes(CommandLCAdmin::listTraderData))
						.then(Commands.literal("search")
								.then(Commands.argument("searchText", StringArgumentType.greedyString())
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
								.then(Commands.argument("wallet", ItemArgument.item(context))
										.executes(CommandLCAdmin::replaceWalletSlotWithDefault)
										.then(Commands.argument("keepWalletContents", BoolArgumentType.bool())
												.executes(CommandLCAdmin::replaceWalletSlot)))))
				.then(Commands.literal("taxes")
						.then(Commands.literal("list")
								.executes(CommandLCAdmin::listTaxCollectors))
						.then(Commands.literal("delete")
								.then(Commands.argument("taxCollectorID", LongArgumentType.longArg(0))
										.executes(CommandLCAdmin::deleteTaxCollector)))
						.then(Commands.literal("openServerTax")
								.requires(CommandSourceStack::isPlayer)
								.executes(CommandLCAdmin::openServerTax))
						.then(Commands.literal("forceDisableTaxCollectors")
								.executes(CommandLCAdmin::forceDisableTaxCollectors)))
				.then(Commands.literal("events")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.literal("list")
										.executes(CommandLCAdmin::listUnlockedEvents))
								.then(Commands.literal("unlock")
										.then(Commands.argument("event", StringArgumentType.word())
											.executes(CommandLCAdmin::unlockEvent)))
								.then(Commands.literal("lock")
										.then(Commands.argument("event", StringArgumentType.word())
											.executes(CommandLCAdmin::lockEvent)))));

		dispatcher.register(lcAdminCommand);

	}

	static int toggleAdmin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{

		CommandSourceStack source = commandContext.getSource();
		ServerPlayer sourcePlayer = source.getPlayerOrException();

		LCAdminMode.ToggleAdminPlayer(sourcePlayer);
		Component enabledDisabled = LCAdminMode.isAdminPlayer(sourcePlayer) ? EasyText.translatable("command.lightmanscurrency.lcadmin.toggleadmin.enabled").withStyle(ChatFormatting.GREEN) : EasyText.translatable("command.lightmanscurrency.lcadmin.toggleadmin.disabled").withStyle(ChatFormatting.RED);
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

	static int searchTraderData(CommandContext<CommandSourceStack> commandContext) {

		CommandSourceStack source = commandContext.getSource();

		String searchText = StringArgumentType.getString(commandContext,"searchText");

		List<TraderData> results = TraderSaveData.GetAllTraders(false).stream().filter(trader -> TraderAPI.filterTrader(trader, searchText)).toList();
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
		if(source.getEntity() != null && source.getEntity() instanceof Player player)
			new SPacketDebugTrader(trader.getID()).sendTo(player);
		return 1;

	}

	static int debugTraderData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		CommandSourceStack source = commandContext.getSource();

		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");
		EasyText.sendCommandSucess(source, EasyText.literal(trader.save().getAsString()), false);
		if(commandContext.getSource().isPlayer())
			new SPacketDebugTrader(trader.getID()).sendTo(commandContext.getSource().getPlayerOrException());
		return 1;
	}

	static int addToTraderWhitelist(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{

		CommandSourceStack source = commandContext.getSource();

		TraderData trader = TraderArgument.getTrader(commandContext, "traderID");

		TradeRule rule = TradeRule.getRule(PlayerWhitelist.TYPE.type, trader.getRules());
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
						newWallet.setTag(oldWallet.getOrCreateTag().copy());
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

	static int listTaxCollectors(CommandContext<CommandSourceStack> commandContext)
	{
		CommandSourceStack source = commandContext.getSource();
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.title"), false);
		for(TaxEntry entry : TaxSaveData.GetAllTaxEntries(false))
			sendTaxDataFeedback(entry, source);
		return 1;
	}

	private static void sendTaxDataFeedback(TaxEntry taxEntry, CommandSourceStack source)
	{
		if(taxEntry.isServerEntry())
			return;
		//Trader ID
		String id = String.valueOf(taxEntry.getID());
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.id", EasyText.literal(id).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.id.copytooltip"))))), false);

		//Team / Team ID
		if(taxEntry.getOwner().hasTeam())
		{
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.owner.team", taxEntry.getOwner().getTeam().getName(), taxEntry.getOwner().getTeam().getID()), false);
		}
		//Owner / Owner ID
		else if(taxEntry.getOwner().hasPlayer())
		{
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.owner", taxEntry.getOwner().getPlayer().getName(false), taxEntry.getOwner().getPlayer().id.toString()), false);
		}

		//Dimension
		String dimension = taxEntry.getArea().getCenter().getDimension().toString();
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.dimension", dimension), false);
		//Position
		BlockPos pos = taxEntry.getArea().getCenter().getPos();
		String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
		String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
		EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.position", EasyText.literal(position).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.position.teleporttooltip"))))), true);
		//Range
		if(taxEntry.isInfiniteRange())
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.infinite_range"), false);
		else
		{
			//Radius
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.radius", taxEntry.getRadius()), false);
			//Height
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.height", taxEntry.getHeight()), false);
			//Vertical Offset
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.offset", taxEntry.getVertOffset()), false);
		}
		if(taxEntry.forcesAcceptance())
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.force_acceptance"), false);

		//Custom Name (if applicable)
		if(taxEntry.hasCustomName())
			EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.list.name", taxEntry.getName()), true);
	}

	static int deleteTaxCollector(CommandContext<CommandSourceStack> commandContext)
	{
		long taxCollectorID = LongArgumentType.getLong(commandContext, "taxCollectorID");

		TaxEntry entry = TaxSaveData.GetTaxEntry(taxCollectorID, false);
		if(entry == null || entry.isServerEntry())
		{
			EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.delete.fail"));
			return 0;
		}
		TaxSaveData.RemoveEntry(taxCollectorID);
		EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.taxdata.delete.success", entry.getName()), true);
		return 1;
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


	static int listUnlockedEvents(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		Player player = EntityArgument.getPlayer(commandContext, "player");

		IEventUnlocks eventUnlocks = CapabilityEventUnlocks.getCapability(player);
		if(eventUnlocks != null)
		{
			List<String> unlocks = eventUnlocks.getUnlockedList();
			if(unlocks.size() > 0)
			{
				StringBuilder list = new StringBuilder();
				for(String v : eventUnlocks.getUnlockedList())
				{
					if(!list.isEmpty())
						list.append(", ");
					list.append(v);
				}
				EasyText.sendCommandSucess(commandContext.getSource(), EasyText.literal(list.toString()), false);
			}
			else
				EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.event.list.none"), false);
			return 1;
		}
		else
			return 0;
	}

	static int unlockEvent(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		Player player = EntityArgument.getPlayer(commandContext, "player");

		String event = StringArgumentType.getString(commandContext, "event");

		if(CapabilityEventUnlocks.isUnlocked(player, event))
		{
			EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.event.unlock.fail", event));
			return 0;
		}

		CapabilityEventUnlocks.unlock(player, event);
		EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.event.unlock.success", event), false);

		return 1;
	}

	static int lockEvent(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		Player player = EntityArgument.getPlayer(commandContext, "player");

		String event = StringArgumentType.getString(commandContext, "event");

		if(!CapabilityEventUnlocks.isUnlocked(player, event))
		{
			EasyText.sendCommandFail(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.event.lock.fail", event));
			return 0;
		}

		CapabilityEventUnlocks.lock(player, event);
		EasyText.sendCommandSucess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcadmin.event.lock.success", event), false);

		return 1;
	}

}
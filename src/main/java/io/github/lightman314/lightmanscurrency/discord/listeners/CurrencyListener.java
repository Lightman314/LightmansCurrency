package io.github.lightman314.lightmanscurrency.discord.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.IBarterTrade;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.discord.events.DiscordTraderSearchEvent;
import io.github.lightman314.lightmanscurrency.common.events.AuctionHouseEvent.AuctionEvent.AuctionCompletedEvent;
import io.github.lightman314.lightmanscurrency.common.events.AuctionHouseEvent.AuctionEvent.CancelAuctionEvent;
import io.github.lightman314.lightmanscurrency.common.events.AuctionHouseEvent.AuctionEvent.CreateAuctionEvent;
import io.github.lightman314.lightmanscurrency.common.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.common.events.TraderEvent.CreateNetworkTraderEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CurrencyListener extends SingleChannelListener{
	
	private final Timer timer;
	
	private static final long PENDING_MESSAGE_TIMER = 300000; //5m timer cycle for sending pending messages.
	private static final long ANNOUCEMENT_DELAY = 60000; //60s delay before announcing to give the owner time to set a name, etc.
	
	Map<String,List<String>> pendingMessages = new HashMap<>();
	
	public CurrencyListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel, LightmansDiscordIntegration.PROXY::getJDA);
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new NotifyTraderOwnerTask(this), 0, PENDING_MESSAGE_TIMER);
	}

	@Override
	protected void onChannelMessageReceived(MessageReceivedEvent event) {
		handleMessage(event.getChannel(), event.getMessage(), event.getAuthor());
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getChannelType() == ChannelType.PRIVATE)
			this.handleMessage(event.getChannel(), event.getMessage(), event.getAuthor());
		else
			super.onMessageReceived(event);
	}
	
	private void handleMessage(MessageChannel channel, Message message, User author)
	{
		if(author.isBot())
			return;
		
		//Run command
		String input = message.getContentDisplay();
		String prefix = Config.SERVER.currencyCommandPrefix.get();
		if(input.startsWith(prefix))
		{
			String command = input.substring(prefix.length());
			if(command.startsWith("help"))
			{
				List<String> output = new ArrayList<>();
				output.add(prefix + "notifications <help|enable|disable> - " + CurrencyMessages.M_HELP_LC_NOTIFICATIONS.get());
				output.add(prefix + "search <sales|purchases|barters|trades> [searchText] - " + CurrencyMessages.M_HELP_LC_SEARCH1.get());
				output.add(prefix + "search <players|shops> [searchText] - " + CurrencyMessages.M_HELP_LC_SEARCH2.get());
				output.add(prefix + "search all - " + CurrencyMessages.M_HELP_LC_SEARCH3.get());
				MessageUtil.sendTextMessage(channel, output);
			}
			else if(command.startsWith("notifications "))
			{
				String subcommand = command.substring(14);
				if(subcommand.startsWith("help"))
				{
					List<String> output = new ArrayList<>();
					if(AccountManager.getLinkedAccountFromUser(author) == null)
						output.add(CurrencyMessages.M_NOTIFICATIONS_NOTLINKED.get());
					else if(AccountManager.currencyNotificationsEnabled(author))
						output.add(CurrencyMessages.M_NOTIFICATIONS_ENABLED.get());
					else
						output.add(CurrencyMessages.M_NOTIFICATIONS_DISABLED.get());
					output.addAll(Lists.newArrayList(CurrencyMessages.M_NOTIFICATIONS_HELP.get().split("\n")));
					
					MessageUtil.sendTextMessage(channel, output);
				}
				else if(subcommand.startsWith("enable"))
				{
					if(AccountManager.getLinkedAccountFromUser(author) == null)
						MessageUtil.sendTextMessage(channel, MessageManager.M_ERROR_NOTLINKEDSELF.get());
					else if(AccountManager.enableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, CurrencyMessages.M_NOTIFICATIONS_ENABLE_SUCCESS.get());
					else
						MessageUtil.sendTextMessage(channel, CurrencyMessages.M_NOTIFICATIONS_ENABLE_FAIL.get());
				}
				else if(subcommand.startsWith("disable"))
				{
					if(AccountManager.getLinkedAccountFromUser(author) == null)
						MessageUtil.sendTextMessage(channel, MessageManager.M_ERROR_NOTLINKEDSELF.get());
					else if(AccountManager.disableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, CurrencyMessages.M_NOTIFICATIONS_DISABLE_SUCCESS.get());
					else
						MessageUtil.sendTextMessage(channel, CurrencyMessages.M_NOTIFICATIONS_DISABLE_FAIL.get());
				}
			}
			else if(command.startsWith("search "))
			{
				String subcommand = command.substring(7);
				String text = "";
				SearchCategory type = null;
				if(subcommand.startsWith("sales"))
				{
					type = SearchCategory.TRADE_SALE;
					if(subcommand.length() > 6)
						text = subcommand.substring(6).toLowerCase();
				}
				else if(subcommand.startsWith("purchases"))
				{
					type = SearchCategory.TRADE_PURCHASE;
					if(subcommand.length() > 10)
						text = subcommand.substring(10).toLowerCase();
				}
				else if(subcommand.startsWith("barters"))
				{
					type = SearchCategory.TRADE_BARTER;
					if(subcommand.length() > 10)
						text = subcommand.substring(10).toLowerCase();
				}
				else if(subcommand.startsWith("trades"))
				{
					type = SearchCategory.TRADE_ANY;
					if(subcommand.length() > 7)
						text = subcommand.substring(7).toLowerCase();
				}
				else if(subcommand.startsWith("players"))
				{
					type = SearchCategory.TRADER_OWNER;
					if(subcommand.length() > 8)
						text = subcommand.substring(8).toLowerCase();
				}
				else if(subcommand.startsWith("shops"))
				{
					type = SearchCategory.TRADER_NAME;
					if(subcommand.length() > 6)
						text = subcommand.substring(6).toLowerCase();
				}
				
				else if(subcommand.startsWith("all"))
				{
					type = SearchCategory.TRADER_ANY;
				}
				if(type == null)
				{
					MessageUtil.sendTextMessage(channel, CurrencyMessages.M_SEARCH_BAD_INPUT.get());
					return;
				}
				
				final SearchCategory searchType = type;
				final String searchText = text;
				List<String> output = new ArrayList<>();
				List<TraderData> traderList = Config.SERVER.limitSearchToNetworkTraders.get() ? TraderSaveData.GetAllTerminalTraders(false) : TraderSaveData.GetAllTraders(false);
				traderList.forEach(trader -> {
					try {
						if(trader instanceof ItemTraderData itemTrader)
						{
							if(searchType.acceptTrader(itemTrader, searchText))
							{
								boolean showStock = !itemTrader.isCreative();
								boolean firstTrade = true;
								for(int i = 0; i < itemTrader.getTradeCount(); ++i)
								{
									ItemTradeData trade = itemTrader.getTrade(i);
									if(trade.isValid())
									{
										if(trade.isSale())
										{
											String itemName1 = getItemName(trade.getSellItem(0), trade.getCustomName(0));
											String itemName2 = getItemName(trade.getSellItem(1), trade.getCustomName(0));
											
											if(!searchType.filterByTrade() || searchText.isEmpty() || itemName1.toLowerCase().contains(searchText) || itemName2.toLowerCase().contains(searchText))
											{
												if(firstTrade)
												{
													output.add("--" + itemTrader.getOwner().getOwnerName(false) + "'s **" + itemTrader.getName().getString() + "**--");
													firstTrade = false;
												}
												String priceText = trade.getCost().getString();
												output.add("Selling " + getItemNamesAndCount(trade.getSellItem(0), trade.getCustomName(0), trade.getSellItem(1), trade.getCustomName(1)) + " for " + priceText);
												if(showStock)
													output.add("*" + trade.stockCount(itemTrader) + " trades in stock.*");
											}
										}
										else if(trade.isPurchase())
										{
											String itemName1 = getItemName(trade.getSellItem(0), "");
											String itemName2 = getItemName(trade.getSellItem(1), "");
											
											if(!searchType.filterByTrade() || searchText.isEmpty() || itemName1.toLowerCase().contains(searchText) || itemName2.toLowerCase().contains(searchText))
											{
												if(firstTrade)
												{
													output.add("--" + itemTrader.getOwner().getOwnerName(false) + "'s **" + itemTrader.getName().getString() + "**--");
													firstTrade = false;
												}
												String priceText = trade.getCost().getString();
												output.add("Purchasing " + getItemNamesAndCount(trade.getSellItem(0), "", trade.getSellItem(1), "") + " for " + priceText);
												if(showStock)
													output.add("*" + trade.stockCount(itemTrader) + " trades in stock.*");
											}
										}
										else if(trade.isBarter())
										{
											
											String itemName1 = getItemName(trade.getSellItem(0), trade.getCustomName(0));
											String itemName2 = getItemName(trade.getSellItem(1), trade.getCustomName(1));
											String itemName3 = getItemName(trade.getBarterItem(0), "");
											String itemName4 = getItemName(trade.getBarterItem(1), "");
											
											if(!searchType.filterByTrade() || searchText.isEmpty() || itemName1.toLowerCase().contains(searchText) || itemName2.toLowerCase().contains(searchText) || itemName3.toLowerCase().contains(searchText) || itemName4.toLowerCase().contains(searchText))
											{
												if(firstTrade)
												{
													output.add("--" + itemTrader.getOwner().getOwnerName(false) + "'s **" + itemTrader.getName().getString() + "**--");
													firstTrade = false;
												}
												output.add("Bartering " + getItemNamesAndCount(trade.getBarterItem(0), "", trade.getBarterItem(1), "") + " for " + getItemNamesAndCount(trade.getSellItem(0), trade.getCustomName(0), trade.getSellItem(1), trade.getCustomName(1)));
												if(showStock)
													output.add("*" + trade.stockCount(itemTrader) + " trades in stock.*");
											}
										}
									}
								}
							}
						}
						//else //If not an item trader, post the trader search eventm
							MinecraftForge.EVENT_BUS.post(new DiscordTraderSearchEvent(trader, searchText, searchType, output));
					} catch(Exception e) { e.printStackTrace(); }
				});
				if(output.size() > 0)
					MessageUtil.sendTextMessage(channel, output);
				else
					MessageUtil.sendTextMessage(channel, CurrencyMessages.M_SEARCH_NORESULTS.get());
			}
		}
	}
	
	private static String getItemName(ItemStack item, String customName)
	{
		if(item.isEmpty())
			return "";
		//Ignore custom names on purchases
		StringBuffer itemName = new StringBuffer();
		if(customName.isEmpty())
			itemName.append(item.getHoverName().getString());
		else
			itemName.append("*").append(customName).append("*");
		//Get enchantment data (if present)
		AtomicBoolean firstEnchantment = new AtomicBoolean(true);
		EnchantmentHelper.getEnchantments(item).forEach((enchantment, level) ->{
			if(firstEnchantment.get())
			{
				itemName.append(" [").append(enchantment.getFullname(level).getString());
				firstEnchantment.set(false);
			}
			else
				itemName.append(", ").append(enchantment.getFullname(level).getString());
		});
		if(!firstEnchantment.get()) //If an enchantment was gotten, append the end
			itemName.append("]");
		
		return itemName.toString();
	}
	
	private static String getItemNamesAndCounts(List<ItemStack> items) {
		List<String> itemEntries = new ArrayList<>();
		for(ItemStack item : items)
		{
			if(!item.isEmpty())
				itemEntries.add(item.getCount() + "x " + getItemName(item,""));
		}
		if(itemEntries.size() > 0)
		{
			if(itemEntries.size() == 2)
			{
				return itemEntries.get(0) + " and " + itemEntries.get(1);
			}
			else
			{
				StringBuilder buffer = new StringBuilder();
				for(int i = 0; i < itemEntries.size(); ++i)
				{
					if(i != 0)
						buffer.append(", ");
					if(i == itemEntries.size() - 1 && itemEntries.size() > 1)
						buffer.append("and ");
					buffer.append(itemEntries.get(i));
				}
				return buffer.toString();
			}
		}
		else
			return "NULL";
	}
	
	public static String getItemNamesAndCount(ItemStack item1, String customName1, ItemStack item2, String customName2)
	{
		if(item1.isEmpty() && !item2.isEmpty())
			return item2.getCount() + "x " + getItemName(item2, customName2);
		if(item2.isEmpty() && !item1.isEmpty())
			return item1.getCount() + "x " + getItemName(item1, customName1);
		return item1.getCount() + "x " + getItemName(item1, customName1) + " and " + item2.getCount() + "x " + getItemName(item2, customName2);
	}
	
	@SubscribeEvent
	public void onNotification(NotificationEvent.NotificationSent.Post event) {
		try {
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayerID(event.getPlayerID());
			if(account != null)
			{
				User user = account.getUser();
				if(AccountManager.currencyNotificationsEnabled(user))
				{
					this.addPendingMessage(user, event.getNotification().getGeneralMessage().getString());
				}
			}
		} catch(Exception e) { LightmansCurrency.LogError("Error processing notification to bot:", e); }
	}
	
	@SubscribeEvent
	public void onAuctionCreated(CreateAuctionEvent.Post event) {
		if(!Config.SERVER.auctionHouseCreateNotifications.get())
			return;
		if(event.isPersistent() && !Config.SERVER.auctionHouseCreatePersistentNotifications.get())
			return;
		
		AuctionTradeData auction = event.getAuction();
		
		String itemText = getItemNamesAndCounts(auction.getAuctionItems());
		String startingBid = auction.getLastBidAmount().getString();
		String minBid = auction.getMinBidDifference().getString();
		
		if(event.isPersistent())
		{
			this.sendTextMessage(CurrencyMessages.M_NEWAUCTION_PERSISTENT.format(itemText, startingBid, minBid));
		}
		else
		{
			PlayerReference owner = auction.getOwner();
			String ownerName = owner != null ? owner.getName(false) : "NULL";
			this.sendTextMessage(CurrencyMessages.M_NEWAUCTION.format(ownerName, itemText, startingBid, minBid));
		}
		
	}
	
	@SubscribeEvent
	public void onAuctionCanceled(CancelAuctionEvent event) {
		
		if(!Config.SERVER.auctionHouseCancelNotifications.get())
			return;
		
		this.sendTextMessage(CurrencyMessages.M_CANCELAUCTION.format(event.getPlayer().getDisplayName().getString(), getItemNamesAndCounts(event.getAuction().getAuctionItems())));
		
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onAuctionCompleted(AuctionCompletedEvent event) {
		if(!Config.SERVER.auctionHouseWinNotifications.get() || !event.hadBidder())
			return;
		
		AuctionTradeData auction = event.getAuction();
		
		if(auction.getLastBidPlayer() == null)
			return;
		
		String winner = auction.getLastBidPlayer().getName(false);
		String itemText = getItemNamesAndCounts(auction.getAuctionItems());
		String price = auction.getLastBidAmount().getString();
		
		this.sendTextMessage(CurrencyMessages.M_WINAUCTION.format(winner, itemText, price));
		
	}
	
	public void addPendingMessage(User user, String message)
	{
		this.addPendingMessage(user, Lists.newArrayList(message));
	}
	
	public void addPendingMessage(User user, List<String> messages)
	{
		String userId = user.getId();
		List<String> pendingMessages = this.pendingMessages.containsKey(userId) ? this.pendingMessages.get(userId) : Lists.newArrayList();
		pendingMessages.addAll(messages);
		this.pendingMessages.put(userId, pendingMessages);
	}
	
	public void sendPendingMessages()
	{
		//LightmansConsole.LOGGER.info("Sending Pending Messages");
		this.pendingMessages.forEach((userId, messages)->{
			try {
				User user = this.getJDA().getUserById(userId);
				if(user != null)
				{
					MessageUtil.sendPrivateMessage(user, messages);
				}	
			} catch(Exception e) { e.printStackTrace(); }
		});
		this.pendingMessages.clear();
	}
	
	@SubscribeEvent
	public void onUniversalTraderRegistered(CreateNetworkTraderEvent event)
	{
		if(!Config.SERVER.traderCreationNotifications.get())
			return;
		//Announce the creation of the trader 60s later
		new Timer().schedule(new AnnouncementTask(this, event), ANNOUCEMENT_DELAY);
	}
	
	@SubscribeEvent
	public void onServerStop(ServerStoppingEvent event)
	{
		//Cancel the timer
		this.timer.cancel();
		this.sendPendingMessages();
	}
	
	private static class NotifyTraderOwnerTask extends TimerTask
	{
		private final CurrencyListener cl;
		public NotifyTraderOwnerTask(CurrencyListener cl) { this.cl = cl; }
		@Override
		public void run() { this.cl.sendPendingMessages(); }
	}

	private static class AnnouncementTask extends TimerTask
	{
		
		private final CurrencyListener cl;
		private final CreateNetworkTraderEvent event;

		public AnnouncementTask(CurrencyListener cl, CreateNetworkTraderEvent event) {
			this.cl = cl;
			this.event = event;
		}
		
		@Override
		public void run() {
			try {
				TraderData trader = this.event.getTrader();
				if(trader == null) //Abort if the trader was removed.
					return;
				if(trader.hasCustomName())
					cl.sendTextMessage(CurrencyMessages.M_NEWTRADER_NAMED.format(trader.getOwner().getOwnerName(false), trader.getCustomName()));
				else
					cl.sendTextMessage(CurrencyMessages.M_NEWTRADER.format(trader.getOwner().getOwnerName(false)));
			} catch(Exception e) { e.printStackTrace(); }
		}
		
	}
	
	public enum SearchCategory
	{
		TRADE_SALE(trade -> trade.getTradeDirection() == TradeDirection.SALE),
		TRADE_PURCHASE(trade -> trade.getTradeDirection() == TradeDirection.PURCHASE),
		TRADE_BARTER(trade -> { if(trade instanceof IBarterTrade) return ((IBarterTrade)trade).isBarter(); return false; }),
		TRADE_ANY(trade -> true),
		
		TRADER_OWNER((trader,search) -> search.isEmpty() || trader.getOwner().getOwnerName(false).toLowerCase().contains(search)),
		TRADER_NAME((trader,search) -> search.isEmpty() || trader.getName().getString().toLowerCase().contains(search)),
		TRADER_ANY((trader,search) -> true);
		
		private final boolean filterByTrade;
		public boolean filterByTrade() { return this.filterByTrade; }
		
		private final Function<TradeData,Boolean> tradeFilter;
		public boolean acceptTradeType(TradeData trade) { return this.tradeFilter.apply(trade); }
		
		private final BiFunction<TraderData,String,Boolean> acceptTrader;
		public boolean acceptTrader(TraderData trader, String searchText) { return this.acceptTrader.apply(trader, searchText); }
		
		SearchCategory(Function<TradeData,Boolean> tradeFilter) {
			this.filterByTrade = true;
			this.tradeFilter = tradeFilter;
			this.acceptTrader = (t,s) -> true;
		}
		
		SearchCategory(BiFunction<TraderData,String,Boolean> acceptTrader) {
			this.filterByTrade = false;
			this.tradeFilter = (t) -> true;
			this.acceptTrader = acceptTrader;
		}
		
	}
	
}

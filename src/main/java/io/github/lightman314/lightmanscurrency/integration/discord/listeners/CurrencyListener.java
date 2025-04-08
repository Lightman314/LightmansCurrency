package io.github.lightman314.lightmanscurrency.integration.discord.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.integration.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.integration.discord.data.CurrencyBotData;
import io.github.lightman314.lightmanscurrency.integration.discord.data.CurrencyBotSaveData;
import io.github.lightman314.lightmanscurrency.integration.discord.events.DiscordTraderSearchEvent;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.AuctionCompletedEvent;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.CancelAuctionEvent;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.CreateAuctionEvent;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent.CreateNetworkTraderEvent;
import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeUserReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafeMessageChannelReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafePrivateChannelReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.messages.SafeMessageReference;
import io.github.lightman314.lightmansdiscord.api.jda.listeners.SafeSingleChannelListener;
import io.github.lightman314.lightmansdiscord.discord.links.AccountManager;
import io.github.lightman314.lightmansdiscord.discord.links.LinkedAccount;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CurrencyListener extends SafeSingleChannelListener {
	
	private final Timer timer;
	
	private static final long PENDING_MESSAGE_TIMER = 300000; //5m timer cycle for sending pending messages.
	private static final long ANNOUCEMENT_DELAY = 60000; //60s delay before announcing to give the owner time to set a name, etc.
	
	Map<String,List<String>> pendingMessages = new HashMap<>();

	@Override
	protected boolean listenToPrivateMessages() { return true; }

	public CurrencyListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel::get);
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new NotifyTraderOwnerTask(this), 0, PENDING_MESSAGE_TIMER);
	}

	@Override
	protected void OnPrivateMessage(SafePrivateChannelReference channel, SafeUserReference user, SafeMessageReference message) {
		this.handleMessage(channel, message, user);
	}

	@Override
	public void OnTextChannelMessage(SafeMemberReference member, SafeMessageReference message) {
		this.handleMessage(this.getChannel(), message, member);
	}
	
	private void handleMessage(SafeMessageChannelReference channel, SafeMessageReference message, SafeUserReference user)
	{
		if(user.isBot())
			return;
		
		//Run command
		String input = message.getDisplay();
		String prefix = LCConfig.SERVER.ldiCurrencyCommandPrefix.get();
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
				channel.sendMessage(output);
			}
			else if(command.startsWith("notifications "))
			{
				String subcommand = command.substring(14);
				if(subcommand.startsWith("help"))
				{
					List<String> output = new ArrayList<>();
					LinkedAccount account = AccountManager.getLinkedAccountFromUser(user);
					if(account == null)
						output.add(CurrencyMessages.M_NOTIFICATIONS_NOTLINKED.get());
					else if(CurrencyBotSaveData.getDataFor(account).sendNotificationsToDiscord())
						output.add(CurrencyMessages.M_NOTIFICATIONS_ENABLED.get());
					else
						output.add(CurrencyMessages.M_NOTIFICATIONS_DISABLED.get());
					output.addAll(Lists.newArrayList(CurrencyMessages.M_NOTIFICATIONS_HELP.get().split("\n")));

					channel.sendMessage(output);
				}
				else if(subcommand.startsWith("enable"))
				{
					LinkedAccount account = AccountManager.getLinkedAccountFromUser(user);
					if(account == null)
						channel.sendMessage(MessageManager.M_ERROR_NOTLINKEDSELF.get());
					else
					{
						CurrencyBotData data = CurrencyBotSaveData.getDataFor(account);
						if(data.sendNotificationsToDiscord())
							channel.sendMessage(CurrencyMessages.M_NOTIFICATIONS_ENABLE_FAIL.get());
						else
						{
							data.setNotificationsToDiscord(true);
							channel.sendMessage(CurrencyMessages.M_NOTIFICATIONS_ENABLE_SUCCESS.get());
						}

					}
				}
				else if(subcommand.startsWith("disable"))
				{
					LinkedAccount account = AccountManager.getLinkedAccountFromUser(user);
					if(account == null)
						channel.sendMessage(MessageManager.M_ERROR_NOTLINKEDSELF.get());
					else
					{
						CurrencyBotData data = CurrencyBotSaveData.getDataFor(account);
						if(!data.sendNotificationsToDiscord())
							channel.sendMessage(CurrencyMessages.M_NOTIFICATIONS_DISABLE_FAIL.get());
						else
						{
							data.setNotificationsToDiscord(false);
							channel.sendMessage(CurrencyMessages.M_NOTIFICATIONS_DISABLE_SUCCESS.get());
						}

					}
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
					channel.sendMessage(CurrencyMessages.M_SEARCH_BAD_INPUT.get());
					return;
				}
				
				final SearchCategory searchType = type;
				final String searchText = text;

				List<String> output = new ArrayList<>();
				List<TraderData> traderList = LCConfig.SERVER.ldiLimitSearchToNetworkTraders.get() ? TraderAPI.API.GetAllNetworkTraders(false) : TraderAPI.API.GetAllTraders(false);
				traderList.forEach(trader -> {
					try {
						if(searchType.acceptTrader(trader, searchText))
							MinecraftForge.EVENT_BUS.post(new DiscordTraderSearchEvent(trader, searchText, searchType, output));
					} catch(Throwable e) { LightmansCurrency.LogError("Error during the DiscordTraderSearchEvent!", e); }
				});
				if(!output.isEmpty())
					channel.sendMessage(output);
				else
					channel.sendMessage(CurrencyMessages.M_SEARCH_NORESULTS.get());
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
				itemEntries.add(CurrencyMessages.M_SEARCH_TRADE_ITEM_SINGLE.format(item.getCount(), getItemName(item,"")));
		}
		if(!itemEntries.isEmpty())
		{
			if(itemEntries.size() == 2)
			{
				return itemEntries.get(0) + CurrencyMessages.M_SEARCH_TRADE_ITEM_DOUBLE + itemEntries.get(1);
			}
			else
			{
				StringBuilder buffer = new StringBuilder();
				for(int i = 0; i < itemEntries.size(); ++i)
				{
					if(i != 0)
						buffer.append(CurrencyMessages.M_SEARCH_TRADE_ITEM_LIST);
					if(i == itemEntries.size() - 1 && itemEntries.size() > 1)
						buffer.append(CurrencyMessages.M_SEARCH_TRADE_ITEM_DOUBLE);
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
			return CurrencyMessages.M_SEARCH_TRADE_ITEM_SINGLE.format(item2.getCount(),getItemName(item2, customName2));
		if(item2.isEmpty() && !item1.isEmpty())
			return CurrencyMessages.M_SEARCH_TRADE_ITEM_SINGLE.format(item1.getCount(),getItemName(item1, customName1));
		return CurrencyMessages.M_SEARCH_TRADE_ITEM_SINGLE.format(item1.getCount(),getItemName(item1, customName1)) + CurrencyMessages.M_SEARCH_TRADE_ITEM_DOUBLE.get() + CurrencyMessages.M_SEARCH_TRADE_ITEM_SINGLE.format(item2.getCount(),getItemName(item2, customName2));
	}
	
	@SubscribeEvent
	public void onNotification(NotificationEvent.NotificationSent.Post event) {
		try {
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayerID(event.getPlayerID());
			if(account != null)
			{
				SafeUserReference user = account.getUser();
				if(CurrencyBotSaveData.getDataFor(account).sendNotificationsToDiscord())
					this.addPendingMessage(user.getUser(), event.getNotification().getGeneralMessage().stream().map(Component::getString).toList());
			}
		} catch(Exception e) { LightmansCurrency.LogError("Error processing notification to bot:", e); }
	}

	@SubscribeEvent
	public void onTraderSearch(DiscordTraderSearchEvent event)
	{
		TraderData trader = event.getTrader();
		String searchText = event.getSearchText();
		if(trader instanceof ItemTraderData itemTrader)
		{
			boolean showStock = !itemTrader.isCreative();
			boolean firstTrade = true;
			for(int i = 0; i < itemTrader.getTradeCount(); ++i)
			{
				ItemTradeData trade = itemTrader.getTrade(i);
				if(trade.isValid() && event.acceptTradeType(trade))
				{
					if(trade.isSale())
					{
						String itemName1 = getItemName(trade.getSellItem(0), trade.getCustomName(0));
						String itemName2 = getItemName(trade.getSellItem(1), trade.getCustomName(1));

						if(searchText.isEmpty() || itemName1.toLowerCase().contains(searchText) || itemName2.toLowerCase().contains(searchText))
						{
							if(firstTrade)
							{
								event.addToOutput(CurrencyMessages.M_SEARCH_TRADER_NAME.format(itemTrader.getOwner().getName(), trader.getName()));
								firstTrade = false;
							}
							String priceText = trade.getCost().getString();
							event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_ITEM_SALE.format(getItemNamesAndCount(trade.getSellItem(0), trade.getCustomName(0), trade.getSellItem(1), trade.getCustomName(1)), priceText));
							if(showStock)
								event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.stockCount(itemTrader)));
						}
					}
					else if(trade.isPurchase())
					{
						String itemName1 = getItemName(trade.getSellItem(0), "");
						String itemName2 = getItemName(trade.getSellItem(1), "");

						if(searchText.isEmpty() || itemName1.toLowerCase().contains(searchText) || itemName2.toLowerCase().contains(searchText))
						{
							if(firstTrade)
							{
								event.addToOutput(CurrencyMessages.M_SEARCH_TRADER_NAME.format(itemTrader.getOwner().getName(), trader.getName()));
								firstTrade = false;
							}
							event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_ITEM_PURCHASE.format(getItemNamesAndCount(trade.getSellItem(0), "", trade.getSellItem(1), ""), trade.getCost().getString()));
							if(showStock)
								event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.stockCount(itemTrader)));
						}
					}
					else if(trade.isBarter())
					{

						String itemName1 = getItemName(trade.getSellItem(0), trade.getCustomName(0));
						String itemName2 = getItemName(trade.getSellItem(1), trade.getCustomName(1));
						String itemName3 = getItemName(trade.getBarterItem(0), "");
						String itemName4 = getItemName(trade.getBarterItem(1), "");

						if(searchText.isEmpty() || itemName1.toLowerCase().contains(searchText) || itemName2.toLowerCase().contains(searchText) || itemName3.toLowerCase().contains(searchText) || itemName4.toLowerCase().contains(searchText))
						{
							if(firstTrade)
							{
								event.addToOutput(CurrencyMessages.M_SEARCH_TRADER_NAME.format(itemTrader.getOwner().getName(), trader.getName()));
								firstTrade = false;
							}
							event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_ITEM_BARTER.format(getItemNamesAndCount(trade.getBarterItem(0), "", trade.getBarterItem(1), ""), getItemNamesAndCount(trade.getSellItem(0), trade.getCustomName(0), trade.getSellItem(1), trade.getCustomName(1))));
							if(showStock)
								event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.stockCount(itemTrader)));
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onAuctionCreated(CreateAuctionEvent.Post event) {
		if(!LCConfig.SERVER.ldiAuctionCreateNotification.get())
			return;
		if(event.isPersistent() && !LCConfig.SERVER.ldiAuctionPersistentCreateNotification.get())
			return;
		
		AuctionTradeData auction = event.getAuction();
		
		String itemText = getItemNamesAndCounts(auction.getAuctionItems());
		String startingBid = auction.getLastBidAmount().getString();
		String minBid = auction.getMinBidDifference().getString();
		
		if(event.isPersistent())
		{
			this.sendMessage(CurrencyMessages.M_NEWAUCTION_PERSISTENT.format(itemText, startingBid, minBid));
		}
		else
		{
			PlayerReference owner = auction.getOwner();
			String ownerName = owner != null ? owner.getName(false) : "NULL";
			this.sendMessage(CurrencyMessages.M_NEWAUCTION.format(ownerName, itemText, startingBid, minBid));
		}
		
	}
	
	@SubscribeEvent
	public void onAuctionCanceled(CancelAuctionEvent event) {
		
		if(!LCConfig.SERVER.ldiAuctionCancelNotification.get())
			return;
		
		this.sendMessage(CurrencyMessages.M_CANCELAUCTION.format(event.getPlayer().getDisplayName().getString(), getItemNamesAndCounts(event.getAuction().getAuctionItems())));
		
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onAuctionCompleted(AuctionCompletedEvent event) {
		if(!LCConfig.SERVER.ldiAuctionWinNotification.get() || !event.hadBidder())
			return;
		
		AuctionTradeData auction = event.getAuction();
		
		if(auction.getLastBidPlayer() == null)
			return;
		
		String winner = auction.getLastBidPlayer().getName(false);
		String itemText = getItemNamesAndCounts(auction.getAuctionItems());
		String price = auction.getLastBidAmount().getString();
		
		this.sendMessage(CurrencyMessages.M_WINAUCTION.format(winner, itemText, price));
		
	}
	
	public void addPendingMessage(User user, String message)
	{
		this.addPendingMessage(user, Lists.newArrayList(message));
	}
	
	public void addPendingMessage(User user, List<String> messages)
	{
		String userId = user.getId();
		List<String> pendingMessages = this.pendingMessages.containsKey(userId) ? this.pendingMessages.get(userId) : new ArrayList<>();
		pendingMessages.addAll(messages);
		this.pendingMessages.put(userId, pendingMessages);
	}
	
	public void sendPendingMessages()
	{
		//LightmansConsole.LOGGER.info("Sending Pending Messages");
		this.pendingMessages.forEach((userId, messages)->{
			try {
				User user = LightmansDiscordIntegration.PROXY.getJDA().getUserById(userId);
				if(user != null)
					SafeUserReference.of(user).sendPrivateMessage(messages);
			} catch(Throwable e) { LightmansCurrency.LogError("Error sending messages!", e); }
		});
		this.pendingMessages.clear();
	}
	
	@SubscribeEvent
	public void onUniversalTraderRegistered(CreateNetworkTraderEvent event)
	{
		if(!LCConfig.SERVER.ldiNetworkTraderNotification.get())
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
					cl.sendMessage(CurrencyMessages.M_NEWTRADER_NAMED.format(trader.getOwner().getName(), trader.customName.get()));
				else
					cl.sendMessage(CurrencyMessages.M_NEWTRADER.format(trader.getOwner().getName()));
			} catch(Exception e) { LightmansCurrency.LogError("Error sending New Trader Announcement", e); }
		}
		
	}
	
	public enum SearchCategory
	{
		TRADE_SALE(trade -> trade.getTradeDirection() == TradeDirection.SALE),
		TRADE_PURCHASE(trade -> trade.getTradeDirection() == TradeDirection.PURCHASE),
		TRADE_BARTER(trade -> trade.getTradeDirection() == TradeDirection.BARTER),
		TRADE_ANY(trade -> true),
		
		TRADER_OWNER((trader,search) -> search.isEmpty() || trader.getOwner().getName().getString().toLowerCase().contains(search)),
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

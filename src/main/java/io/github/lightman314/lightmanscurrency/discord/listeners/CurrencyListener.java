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
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.discord.events.DiscordTraderSearchEvent;
import io.github.lightman314.lightmanscurrency.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.UniversalTradeCreateEvent;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.IBarterTrade;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CurrencyListener extends SingleChannelListener{
	
	private final Timer timer;
	
	private static final long PENDING_MESSAGE_TIMER = 300000; //5m timer cycle for sending pending messages.
	private static final long ANNOUCEMENT_DELAY = 60000; //60s delay before announcing to give the owner time to set a name, etc.
	
	Map<String,List<String>> pendingMessages = new HashMap<>();
	
	public CurrencyListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel, () -> LightmansDiscordIntegration.PROXY.getJDA());
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new NotifyTraderOwnerTask(this), 0, PENDING_MESSAGE_TIMER);
	}

	@Override
	protected void onChannelMessageReceived(MessageReceivedEvent event) {
		
		handleMessage(event.getChannel(), event.getMessage(), event.getAuthor());
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event)
	{
		handleMessage(event.getChannel(), event.getMessage(), event.getAuthor());
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
					output.add(AccountManager.currencyNotificationsEnabled(author) ? CurrencyMessages.M_NOTIFICATIONS_ENABLED.get() : CurrencyMessages.M_NOTIFICATIONS_DISABLED.get());
					output.addAll(Lists.newArrayList(CurrencyMessages.M_NOTIFICATIONS_HELP.get().split("\n")));
					
					MessageUtil.sendTextMessage(channel, output);
				}
				else if(subcommand.startsWith("enable"))
				{
					if(AccountManager.enableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, CurrencyMessages.M_NOTIFICATIONS_ENABLE_SUCCESS.get());
					else
						MessageUtil.sendTextMessage(channel, CurrencyMessages.M_NOTIFICATIONS_ENABLE_FAIL.get());
				}
				else if(subcommand.startsWith("disable"))
				{
					if(AccountManager.disableCurrencyNotifications(author))
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
				TradingOffice.getTraders().forEach(trader -> {
					try {
						if(trader instanceof UniversalItemTraderData)
						{
							UniversalItemTraderData itemTrader = (UniversalItemTraderData)trader;
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
													output.add("--" + itemTrader.getCoreSettings().getOwnerName() + "'s **" + itemTrader.getName().getString() + "**--");
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
													output.add("--" + itemTrader.getCoreSettings().getOwnerName() + "'s **" + itemTrader.getName().getString() + "**--");
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
													output.add("--" + itemTrader.getCoreSettings().getOwnerName() + "'s **" + itemTrader.getName().getString() + "**--");
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
						else //If not an item trader, post the trader search eventm
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
	
	private static String getItemNamesAndCount(ItemStack item1, String customName1, ItemStack item2, String customName2)
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
				if(user != null && AccountManager.currencyNotificationsEnabled(user))
				{
					LightmansCurrency.LogInfo("Adding pending message for user #" + user.getId() + ":\n" + event.getNotification().getMessage().getString());
					this.addPendingMessage(user, event.getNotification().getMessage().getString());
				}
			}
		} catch(Exception e) { LightmansCurrency.LogError("Error processing notification to bot:", e); }
	}
	
	public void addPendingMessage(User user, String message)
	{
		this.addPendingMessage(user, Lists.newArrayList(message));
	}
	
	public void addPendingMessage(User user, List<String> messages)
	{
		String userId = user.getId();
		List<String> pendingMessages = this.pendingMessages.containsKey(userId) ? this.pendingMessages.get(userId) : Lists.newArrayList();
		messages.forEach(message -> pendingMessages.add(message));
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
	public void onUniversalTraderRegistered(UniversalTradeCreateEvent event)
	{
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
		private final UniversalTradeCreateEvent event;

		public AnnouncementTask(CurrencyListener cl, UniversalTradeCreateEvent event) {
			this.cl = cl;
			this.event = event;
		}
		
		@Override
		public void run() {
			try {
				if(this.event.getData() == null) //Abort if the trader was removed.
					return;
				if(event.getData().getCoreSettings().hasCustomName())
					cl.sendTextMessage(CurrencyMessages.M_NEWTRADER_NAMED.format(this.event.getData().getCoreSettings().getOwnerName(), event.getData().getCoreSettings().getCustomName()));
				else
					cl.sendTextMessage(CurrencyMessages.M_NEWTRADER.format(this.event.getData().getCoreSettings().getOwnerName()));
			} catch(Exception e) { e.printStackTrace(); }
		}
		
	}
	
	public enum SearchCategory
	{
		TRADE_SALE(trade -> trade.getTradeDirection() == TradeDirection.SALE),
		TRADE_PURCHASE(trade -> trade.getTradeDirection() == TradeDirection.PURCHASE),
		TRADE_BARTER(trade -> { if(trade instanceof IBarterTrade) return ((IBarterTrade)trade).isBarter(); return false; }),
		TRADE_ANY(trade -> true),
		
		TRADER_OWNER((trader,search) -> search.isEmpty() || trader.getCoreSettings().getOwnerName().toLowerCase().contains(search)),
		TRADER_NAME((trader,search) -> search.isEmpty() || trader.getName().getString().toLowerCase().contains(search)),
		TRADER_ANY((trader,search) -> true);
		
		private final boolean filterByTrade;
		public boolean filterByTrade() { return this.filterByTrade; }
		
		private final Function<TradeData,Boolean> tradeFilter;
		public boolean acceptTradeType(TradeData trade) { return this.tradeFilter.apply(trade); }
		
		private final BiFunction<ITrader,String,Boolean> acceptTrader;
		public boolean acceptTrader(ITrader trader, String searchText) { return this.acceptTrader.apply(trader, searchText); }
		
		SearchCategory(Function<TradeData,Boolean> tradeFilter) {
			this.filterByTrade = true;
			this.tradeFilter = tradeFilter;
			this.acceptTrader = (t,s) -> true;
		}
		
		SearchCategory(BiFunction<ITrader,String,Boolean> acceptTrader) {
			this.filterByTrade = false;
			this.tradeFilter = (t) -> true;
			this.acceptTrader = acceptTrader;
		}
		
	}
	
}

package io.github.lightman314.lightmanscurrency.discord;

import io.github.lightman314.lightmansdiscord.events.LoadMessageEntriesEvent;
import io.github.lightman314.lightmansdiscord.message.MessageEntry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CurrencyMessages {

	private static final List<MessageEntry> ENTRIES = new ArrayList<>();

	//Lightman's Currency !help
	public static final MessageEntry M_HELP_LC_NOTIFICATIONS = MessageEntry.create(ENTRIES, "help_lc_notifications", "Help message for lightman's currency !notifications.", "Handle private currency notifications.");
	public static final MessageEntry M_HELP_LC_SEARCH1 = MessageEntry.create(ENTRIES, "help_lc_search1", "Help message for lightman's currency !search <sales|purchases|barters|trades>.", "List all universal trades selling items containing the searchText.");
	public static final MessageEntry M_HELP_LC_SEARCH2 = MessageEntry.create(ENTRIES, "help_lc_search2", "Help message for lightman's currency !search <players|shops>.", "List all trades for universal traders with player/shop names containing the searchText. Leave searchText empty to see all traders trades.");
	public static final MessageEntry M_HELP_LC_SEARCH3 = MessageEntry.create(ENTRIES, "help_lc_search3", "Help message for lightman's currency !search <all>.", "List all trades.");
	
	//Lightman's Currency Bot
	//!notifications help
	public static final MessageEntry M_NOTIFICATIONS_ENABLED = MessageEntry.create(ENTRIES, "command_notifications_enabled", "Message sent when running !messages help while notifications are enabled.", "Personal notifications are enabled.");
	public static final MessageEntry M_NOTIFICATIONS_DISABLED = MessageEntry.create(ENTRIES, "command_notifications_disabled", "Message sent when running !messages help while notifications are disabled.", "Personal notifications are disabled.");
	public static final MessageEntry M_NOTIFICATIONS_NOTLINKED = MessageEntry.create(ENTRIES, "command_notifications_not_linked", "Message sent when running !messages help when their account is not linked.", "Your account must be linked in order to set your notification preferences.");
	public static final MessageEntry M_NOTIFICATIONS_HELP = MessageEntry.create(ENTRIES, "command_notifications_help", "Remaining message sent when running !messages help.", "If personal notifications are enabled you will receive copies of your in-game notifications via Discord PM.");
	//!notifications enable
	public static final MessageEntry M_NOTIFICATIONS_ENABLE_SUCCESS = MessageEntry.create(ENTRIES, "command_notifications_enable_successs", "Message sent when running !messages enable successfully.", "Personal notifications are now enabled.");
	public static final MessageEntry M_NOTIFICATIONS_ENABLE_FAIL = MessageEntry.create(ENTRIES, "command_notifications_enable_fail", "Message sent when failing to run !messages enable.", "Personal notifications were already enabled.");
	//!notifications disable
	public static final MessageEntry M_NOTIFICATIONS_DISABLE_SUCCESS = MessageEntry.create(ENTRIES, "command_notifications_disable_successs", "Message sent when running !messages disable successfully.", "Personal notifications are now disabled.");
	public static final MessageEntry M_NOTIFICATIONS_DISABLE_FAIL = MessageEntry.create(ENTRIES, "command_notifications_disable_fail", "Message sent when failing to run !messages disable.", "Personal notifications were already disabled.");
	
	//!search
	public static final MessageEntry M_SEARCH_BAD_INPUT = MessageEntry.create(ENTRIES, "command_search_badinput", "Message sent when !search is run with an invalid sub-command (sales,purchases,players, etc.).", "Invalid search type.");
	public static final MessageEntry M_SEARCH_NORESULTS = MessageEntry.create(ENTRIES, "command_search_noresults", "Message sent when !search is run and no search results were found.", "No results found.");
	
	//Trader Announcement
	public static final MessageEntry M_NEWTRADER = MessageEntry.create(ENTRIES, "lightmanscurrency_newtrader", "Announcement made in the currency bot channel when a new universal trader has been made.\n{player} for the traders owner name.", "{player} has made a new Trading Server!", "player");
	public static final MessageEntry M_NEWTRADER_NAMED = MessageEntry.create(ENTRIES, "lightmanscurrency_newtrader_named", "Announcement made in the currency bot channel when a new universal trader with a custom name has been made.\n{player} for the traders owner name.\n{trader} for the traders custom name.", "{player} has made a new Trading Server '{trader}'!", "player", "trader");
	public static final MessageEntry M_NEWAUCTION = MessageEntry.create(ENTRIES, "lightmanscurrency_newauction", "Announcement made in the currency bot channel when a new auction is made.\n{player} for the auctions owner name.\n{items} for the items being sold.\n{startingBid} for the starting bid.\n{minBid} for the minimum bid difference.", "{player} has created an auction selling {items} with a starting bid of {startingBid}!", "player","items","startingBid","minBid");
	public static final MessageEntry M_NEWAUCTION_PERSISTENT = MessageEntry.create(ENTRIES, "lightmanscurrency_newauction_persistent", "Announcement made in the currency bot channel when a new persistent auction is made.\n{items} for the items being sold.\n{startingBid} for the starting bid.\n{minBid} for the minimum bid difference.", "The server has created an auction selling {items} with a starting bid of {startingBid}!", "items","startingBid","minBid");
	public static final MessageEntry M_CANCELAUCTION = MessageEntry.create(ENTRIES, "lightmanscurrency_cancelauction", "Announcement made in the currency bot channel when an auction is canceled.\n{player} for the person who canceled the auction.{items} for the items being sold.\n", "The auction for {items} has been cancelled!", "player","items");
	public static final MessageEntry M_WINAUCTION = MessageEntry.create(ENTRIES, "lightmanscurrency_winauction", "Announcement made in the currency bot channel when a player wins an auction.\n{player} for the highest bidder that won the auction.\n{items} for the items being sold.\n{bid} for the amount paid to win the bid.", "{player} won the auction for {items} with a bid of {bid}!", "player","items","bid");

	@SubscribeEvent
	public static void registerMessages(LoadMessageEntriesEvent event) { event.register(ENTRIES); }

}
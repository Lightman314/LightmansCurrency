package io.github.lightman314.lightmanscurrency.common.traders.permissions;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.entity.player.Player;

public class Permissions {

	public static final String OPEN_STORAGE = "openStorage";
	public static final String CHANGE_NAME = "changeName";
	public static final String EDIT_TRADES = "editTrades";
	public static final String COLLECT_COINS = "collectCoins";
	public static final String STORE_COINS = "storeCoins";
	public static final String EDIT_TRADE_RULES = "editTradeRules";
	public static final String EDIT_SETTINGS = "editSettings";
	public static final String ADD_REMOVE_ALLIES = "addRemoveAllies";
	public static final String EDIT_PERMISSIONS = "editPermissions";
	public static final String VIEW_LOGS = "viewLogs";
	public static final String BREAK_TRADER = "breakTrader";
	public static final String BANK_LINK = "bankLink";
	public static final String NOTIFICATION = "notifications";
	public static final String INTERACTION_LINK = "interactionLink";
	public static final String TRANSFER_OWNERSHIP = "transferOwnership";
	
	public static class InputTrader
	{
		public static final String EXTERNAL_INPUTS = "changeExternalInputs";
	}
	
	public static final String ADMIN_MODE = "LC_ADMIN_MODE";
	
	
	public static final void PermissionWarning(Player player, String action, String permission) { PermissionWarning(player, action, permission, 0, 1); }
	
	public static final void PermissionWarning(Player player, String action, String permission, int hasLevel, int requiredLevel)
	{
		LightmansCurrency.LogWarning(player.getName().getString() + " attempted to " + action + " without the appropriate permission level.\nHas " + permission + " level " + hasLevel + ". Level " + requiredLevel + " required.");
	}
	
}
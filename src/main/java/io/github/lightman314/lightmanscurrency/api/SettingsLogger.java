package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class SettingsLogger extends TextLogger{

	public SettingsLogger() {
		super("SettingsHistory");
	}
	
	public static final Component getEnableDisableText(boolean enabled, ChatFormatting enableFormat, ChatFormatting disableFormat)
	{
		return new TranslatableComponent("log.settings." + (enabled ? "enabled" : "disabled")).withStyle(enabled ? enableFormat : disableFormat);
	}
	
	public static final Component getToFromText(boolean enabled, ChatFormatting... format)
	{
		return new TranslatableComponent("log.settings." + (enabled ? "to" : "from")).withStyle(format);
	}
	
	public static final Component getAddRemoveText(boolean added, ChatFormatting addFormat, ChatFormatting removeFormat)
	{
		return new TranslatableComponent("log.settings." + (added ? "add" : "remove")).withStyle(added ? addFormat : removeFormat);
	}
	
	public static final Component getPlayerName(Player player)
	{
		return new TextComponent(player.getName().getString()).withStyle(ChatFormatting.GREEN);
	}
	
	public static final Component getPlayerName(PlayerReference player)
	{
		return new TextComponent(player.lastKnownName()).withStyle(ChatFormatting.GREEN);
	}
	
	public static final Component format(Object value, ChatFormatting... format)
	{
		return new TextComponent(value.toString()).withStyle(format);
	}
	
	public void LogNameChange(Player player, String oldName, String newName)
	{
		if(player == null)
			return;
		if(oldName.isEmpty())
		{
			//{player} set the name to {newName}
			this.AddLog(new TranslatableComponent("log.settings.changename.set", getPlayerName(player), format(newName, ChatFormatting.GOLD)));
		}
		else if(newName.isEmpty())
		{
			//{player} reset the name to default
			this.AddLog(new TranslatableComponent("log.settings.changename.reset", getPlayerName(player)));
		}
		else
		{
			//{player} changed the name from {oldName} to {newName}
			this.AddLog(new TranslatableComponent("log.settings.changename", getPlayerName(player), format(oldName, ChatFormatting.GOLD), format(newName, ChatFormatting.GOLD)));
		}
		
	}
	
	public void LogOwnerChange(Player player, PlayerReference oldOwner, PlayerReference newOwner)
	{
		if(player == null)
			return;
		if(oldOwner.is(player))
		{
			//{oldOwner} transferred ownership to {newOwner}
			this.AddLog(new TranslatableComponent("log.settings.newowner.passed", getPlayerName(player), getPlayerName(newOwner)));
		}
		else if(newOwner.is(player))
		{
			//{newOwner} claimed ownership from {oldOwner}
			this.AddLog(new TranslatableComponent("log.settings.newowner.taken", getPlayerName(player), getPlayerName(oldOwner)));
		}
		else
		{
			//{player} transfered ownership from {oldOwner} to {newOwner}
			this.AddLog(new TranslatableComponent("log.settings.newowner.transferred", getPlayerName(player), getPlayerName(oldOwner), getPlayerName(newOwner)));
		}
	}
	
	public void LogCreativeToggle(Player player, boolean isNowCreative)
	{
		//{player} {ENABLED/DISABLED} creative mode
		this.AddLog(new TranslatableComponent("log.settings.creativemode", getPlayerName(player), getEnableDisableText(isNowCreative, ChatFormatting.GREEN, ChatFormatting.RED)));
	}
	
	public void LogAddRemoveTrade(Player player, boolean added, int newCount)
	{
		//{player} {added/removed} a trade slot. Trader now has {newCount} trades.
		this.AddLog(new TranslatableComponent("log.settings.addremovetrade", getPlayerName(player), getAddRemoveText(added, ChatFormatting.GOLD, ChatFormatting.GOLD), format(newCount, ChatFormatting.GOLD)));
	}
	
	public void LogAllyChange(Player player, PlayerReference ally, boolean added)
	{
		//{player} {added/removed} {ally} from the list of allies.
		this.AddLog(new TranslatableComponent("log.settings.addremoveally", getPlayerName(player), getAddRemoveText(added, ChatFormatting.GOLD, ChatFormatting.GOLD), getPlayerName(ally), getToFromText(added)));
	}
	
	public void LogAllyPermissionChange(Player player, String permission, Object oldValue, Object newValue)
	{
		//{player} changed allies {permission} value from {oldValue} to {newValue}
		this.AddLog(new TranslatableComponent("log.settings.permission.ally", getPlayerName(player), format(permission, ChatFormatting.GOLD), format(oldValue, ChatFormatting.GOLD), format(newValue, ChatFormatting.GOLD)));
	}
	
	public void LogAllyPermissionChange(Player player, String permission, Object newValue)
	{
		//{player} changed allies {permission} value from {oldValue} to {newValue}
		this.AddLog(new TranslatableComponent("log.settings.permission.ally.simple", getPlayerName(player), format(permission, ChatFormatting.GOLD), format(newValue, ChatFormatting.GOLD)));
	}
	
	public void LogSettingsChange(Player player, String setting, Object oldValue, Object newValue)
	{
		//{player} changed {setting} from {oldValue} to {newValue}
		this.AddLog(new TranslatableComponent("log.settings.change", getPlayerName(player), format(setting, ChatFormatting.GOLD), format(oldValue, ChatFormatting.GOLD), format(newValue, ChatFormatting.GOLD)));
	}
	
	public void LogSettingsChange(Player player, String setting, Object newValue)
	{
		//{player} changed {setting} to {newValue}
		this.AddLog(new TranslatableComponent("log.settings.change.simple", getPlayerName(player), format(setting, ChatFormatting.GOLD), format(newValue, ChatFormatting.GOLD)));
	}
	
	public void LogText(MutableComponent text)
	{
		this.AddLog(text);
	}
	

}

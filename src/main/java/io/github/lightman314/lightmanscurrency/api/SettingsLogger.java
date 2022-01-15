package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class SettingsLogger extends TextLogger{

	public SettingsLogger() {
		super("SettingsHistory");
	}
	
	public static final ITextComponent getEnableDisableText(boolean enabled, TextFormatting enableFormat, TextFormatting disableFormat)
	{
		return new TranslationTextComponent("log.settings." + (enabled ? "enabled" : "disabled")).mergeStyle(enabled ? enableFormat : disableFormat);
	}
	
	public static final ITextComponent getToFromText(boolean enabled, TextFormatting... format)
	{
		return new TranslationTextComponent("log.settings." + (enabled ? "to" : "from")).mergeStyle(format);
	}
	
	public static final ITextComponent getAddRemoveText(boolean added, TextFormatting addFormat, TextFormatting removeFormat)
	{
		return new TranslationTextComponent("log.settings." + (added ? "add" : "remove")).mergeStyle(added ? addFormat : removeFormat);
	}
	
	public static final ITextComponent getPlayerName(PlayerEntity player)
	{
		return new StringTextComponent(player.getName().getString()).mergeStyle(TextFormatting.GREEN);
	}
	
	public static final ITextComponent getTeamName(Team team)
	{
		if(team != null)
			return new StringTextComponent(team.getName()).mergeStyle(TextFormatting.GREEN);
		return new StringTextComponent("NULL");
	}
	
	public static final ITextComponent getPlayerName(PlayerReference player)
	{
		return new StringTextComponent(player.lastKnownName()).mergeStyle(TextFormatting.GREEN);
	}
	
	public static final ITextComponent format(Object value, TextFormatting... format)
	{
		return new StringTextComponent(value.toString()).mergeStyle(format);
	}
	
	public void LogNameChange(PlayerEntity player, String oldName, String newName)
	{
		if(player == null)
			return;
		if(oldName.isEmpty())
		{
			//{player} set the name to {newName}
			this.AddLog(new TranslationTextComponent("log.settings.changename.set", getPlayerName(player), format(newName, TextFormatting.GOLD)));
		}
		else if(newName.isEmpty())
		{
			//{player} reset the name to default
			this.AddLog(new TranslationTextComponent("log.settings.changename.reset", getPlayerName(player)));
		}
		else
		{
			//{player} changed the name from {oldName} to {newName}
			this.AddLog(new TranslationTextComponent("log.settings.changename", getPlayerName(player), format(oldName, TextFormatting.GOLD), format(newName, TextFormatting.GOLD)));
		}
		
	}
	
	public void LogOwnerChange(PlayerEntity player, PlayerReference oldOwner, PlayerReference newOwner)
	{
		if(player == null)
			return;
		if(oldOwner.is(player))
		{
			//{oldOwner} transferred ownership to {newOwner}
			this.AddLog(new TranslationTextComponent("log.settings.newowner.passed", getPlayerName(player), getPlayerName(newOwner)));
		}
		else if(newOwner.is(player))
		{
			//{newOwner} claimed ownership from {oldOwner}
			this.AddLog(new TranslationTextComponent("log.settings.newowner.taken", getPlayerName(player), getPlayerName(oldOwner)));
		}
		else
		{
			//{player} transfered ownership from {oldOwner} to {newOwner}
			this.AddLog(new TranslationTextComponent("log.settings.newowner.transferred", getPlayerName(player), getPlayerName(oldOwner), getPlayerName(newOwner)));
		}
	}
	
	public void LogTeamChange(PlayerEntity player, PlayerReference owner, Team oldTeam, Team newTeam)
	{
		if(player == null || owner == null)
			return;
		if(oldTeam == null)
		{
			if(owner.is(player))
			{
				//{owner} transferred ownership to {newTeam}
				this.AddLog(new TranslationTextComponent("log.settings.newowner.passed", getPlayerName(player), getTeamName(newTeam)));
			}
			else
			{
				//{player} transfered ownership from {owner} to {newTeam}
				this.AddLog(new TranslationTextComponent("log.settings.newowner.transferred", getPlayerName(player), getPlayerName(owner), getTeamName(newTeam)));
			}
		}
		else if(newTeam == null)
		{
			if(owner.is(player))
			{
				//{newOwner} claimed ownership from {oldTeam}
				this.AddLog(new TranslationTextComponent("log.settings.newowner.taken", getPlayerName(player), getTeamName(oldTeam)));
			}
			else
			{
				//{player} transfered ownership from {oldTeam} to {owner}
				this.AddLog(new TranslationTextComponent("log.settings.newowner.transferred", getPlayerName(player), getTeamName(oldTeam), getPlayerName(owner)));
			}
		}
		else
		{
			//{player} transfered ownership from {oldTeam} to {newTeam}
			this.AddLog(new TranslationTextComponent("log.settings.newowner.transferred", getPlayerName(player), getTeamName(oldTeam), getTeamName(newTeam)));
		}
	}
	
	public void LogCreativeToggle(PlayerEntity player, boolean isNowCreative)
	{
		//{player} {ENABLED/DISABLED} creative mode
		this.AddLog(new TranslationTextComponent("log.settings.creativemode", getPlayerName(player), getEnableDisableText(isNowCreative, TextFormatting.GREEN, TextFormatting.RED)));
	}
	
	public void LogAddRemoveTrade(PlayerEntity player, boolean added, int newCount)
	{
		//{player} {added/removed} a trade slot. Trader now has {newCount} trades.
		this.AddLog(new TranslationTextComponent("log.settings.addremovetrade", getPlayerName(player), getAddRemoveText(added, TextFormatting.GOLD, TextFormatting.GOLD), format(newCount, TextFormatting.GOLD)));
	}
	
	public void LogAllyChange(PlayerEntity player, PlayerReference ally, boolean added)
	{
		//{player} {added/removed} {ally} from the list of allies.
		this.AddLog(new TranslationTextComponent("log.settings.addremoveally", getPlayerName(player), getAddRemoveText(added, TextFormatting.GOLD, TextFormatting.GOLD), getPlayerName(ally), getToFromText(added)));
	}
	
	public void LogAllyPermissionChange(PlayerEntity player, String permission, Object oldValue, Object newValue)
	{
		//{player} changed allies {permission} value from {oldValue} to {newValue}
		this.AddLog(new TranslationTextComponent("log.settings.permission.ally", getPlayerName(player), format(permission, TextFormatting.GOLD), format(oldValue, TextFormatting.GOLD), format(newValue, TextFormatting.GOLD)));
	}
	
	public void LogAllyPermissionChange(PlayerEntity player, String permission, Object newValue)
	{
		//{player} changed allies {permission} value from {oldValue} to {newValue}
		this.AddLog(new TranslationTextComponent("log.settings.permission.ally.simple", getPlayerName(player), format(permission, TextFormatting.GOLD), format(newValue, TextFormatting.GOLD)));
	}
	
	public void LogSettingsChange(PlayerEntity player, String setting, Object oldValue, Object newValue)
	{
		//{player} changed {setting} from {oldValue} to {newValue}
		this.AddLog(new TranslationTextComponent("log.settings.change", getPlayerName(player), format(setting, TextFormatting.GOLD), format(oldValue, TextFormatting.GOLD), format(newValue, TextFormatting.GOLD)));
	}
	
	public void LogSettingsChange(PlayerEntity player, String setting, Object newValue)
	{
		//{player} changed {setting} to {newValue}
		this.AddLog(new TranslationTextComponent("log.settings.change.simple", getPlayerName(player), format(setting, TextFormatting.GOLD), format(newValue, TextFormatting.GOLD)));
	}
	
	public void LogText(ITextComponent text)
	{
		this.AddLog(text);
	}
	

}

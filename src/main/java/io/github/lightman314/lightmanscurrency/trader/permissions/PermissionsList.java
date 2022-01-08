package io.github.lightman314.lightmanscurrency.trader.permissions;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public class PermissionsList {

	private final HashMap<String,Integer> permissions = Maps.newHashMap();
	
	private final ITrader trader;
	private final String updateType;
	
	public PermissionsList(ITrader trader, String updateType) { this.trader = trader; this.updateType = updateType; }
	
	public PermissionsList(ITrader trader, String updateType, Map<String,Integer> permissions)
	{
		this(trader,updateType);
		permissions.forEach((permission,level) ->{
			this.setLevel(permission, level);
		});
	}
	
	public int getLevel(String permission)
	{
		if(this.permissions.containsKey(permission))
			return this.permissions.get(permission);
		return 0;
	}
	
	public CompoundTag changeLevel(Player requestor, String permission, int level)
	{
		if(!this.trader.hasPermission(requestor, Permissions.EDIT_PERMISSIONS))
		{
			Settings.PermissionWarning(requestor, "edit permissions", Permissions.EDIT_PERMISSIONS);
			return null;
		}
		
		this.setLevel(permission, level);
		
		CompoundTag updateInfo = Settings.initUpdateInfo(this.updateType);
		updateInfo.putString("permission", permission);
		updateInfo.putInt("level", this.getLevel(permission));
		return updateInfo;
	}
	
	public boolean changeLevel(Player requestor, CompoundTag updateInfo)
	{
		if(!this.trader.hasPermission(requestor, Permissions.EDIT_PERMISSIONS))
		{
			Settings.PermissionWarning(requestor, "edit permissions", Permissions.EDIT_PERMISSIONS);
			return false;
		}
		
		String permission = updateInfo.getString("permission");
		int level = updateInfo.getInt("level");
		
		if(level == this.getLevel(permission))
			return false;
		
		int oldLevel = this.getLevel(permission);
		
		this.setLevel(permission, level);
		
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, this.updateType + "." + permission, oldLevel, this.getLevel(permission));
		
		return true;
	}
	
	private void setLevel(String permission, int level)
	{
		if(level <= 0)
			resetLevel(permission);
		else
			permissions.put(permission, level);
	}
	
	private void resetLevel(String permission)
	{
		if(permissions.containsKey(permission))
			permissions.remove(permission);
	}
	
	public void save(CompoundTag compound, String tag) {
		ListTag list = new ListTag();
		this.permissions.forEach((permission,level) -> {
			CompoundTag thisCompound = new CompoundTag();
			thisCompound.putString("permission", permission);
			thisCompound.putInt("level", level);
			list.add(thisCompound);
		});
		compound.put(tag, list);
	}
	
	public static PermissionsList load(ITrader trader, String updateType, CompoundTag compound, String tag)
	{
		PermissionsList result = new PermissionsList(trader, updateType);
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag thisCompound = list.getCompound(i);
				String permission = thisCompound.getString("permission");
				int level = thisCompound.getInt("level");
				result.setLevel(permission, level);
			}
		}
		return result;
	}
	
}

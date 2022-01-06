package io.github.lightman314.lightmanscurrency.trader.permissions;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class PermissionsList {

	private final HashMap<String,Integer> permissions = Maps.newHashMap();
	
	public PermissionsList() { }
	
	public PermissionsList(Map<String,Integer> permissions)
	{
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
	
	public void setLevel(String permission, int level)
	{
		if(level <= 0)
			resetLevel(permission);
		else
			permissions.put(permission, level);
	}
	
	public void resetLevel(String permission)
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
	
	public static PermissionsList load(CompoundTag compound, String tag)
	{
		PermissionsList result = new PermissionsList();
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

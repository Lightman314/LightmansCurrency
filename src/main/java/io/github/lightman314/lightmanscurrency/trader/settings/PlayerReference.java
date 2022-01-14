package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerReference {

	public final UUID id;
	private String name = "";
	public String lastKnownName() { return this.name; }
	
	private PlayerReference(UUID playerID, String name)
	{
		this.id = playerID;
		this.name = name;
	}
	
	public void tryUpdateName(Player player)
	{
		if(is(player))
			this.name = player.getGameProfile().getName();
	}
	
	public boolean is(PlayerReference player)
	{
		if(player == null)
			return false;
		return is(player.id);
	}
	
	public boolean is(GameProfile profile)
	{
		return is(profile.getId());
	}
	
	public boolean is(UUID entityID)
	{
		if(entityID == null)
			return false;
		return entityID.equals(this.id);
	}
	
	public boolean is(Entity entity)
	{
		if(entity == null)
			return false;
		return entity.getUUID().equals(this.id);
	}
	
	public boolean is(String name)
	{
		return this.name.toLowerCase().contentEquals(name.toLowerCase());
	}
	
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		compound.putUUID("id", this.id);
		compound.putString("name", this.name);
		return compound;
	}
	
	public static PlayerReference load(CompoundTag compound)
	{
		try {
			UUID id = compound.getUUID("id");
			String name = compound.getString("name");
			return new PlayerReference(id, name);
		} catch(Exception e) { LightmansCurrency.LogError("Error loading PlayerReference from tag.", e.getMessage()); return null; }
	}
	
	public static void saveList(CompoundTag compound, List<PlayerReference> playerList, String tag)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < playerList.size(); ++i)
		{
			CompoundTag thisCompound = playerList.get(i).save();
			list.add(thisCompound);
		}
		compound.put(tag, list);
	}
	
	public static List<PlayerReference> loadList(CompoundTag compound, String tag)
	{
		List<PlayerReference> playerList = Lists.newArrayList();
		ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag thisCompound = list.getCompound(i);
			PlayerReference player = load(thisCompound);
			if(player != null)
				playerList.add(player);
		}
		return playerList;
	}
	
	public static PlayerReference of(UUID playerID, String name)
	{
		if(playerID == null)
			return null;
		//Attempt to the the players profile, for latest name updates
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return of(server.getProfileCache().get(playerID).orElse(null));
		return new PlayerReference(playerID, name);
	}
	
	public static PlayerReference of(GameProfile playerProfile)
	{
		if(playerProfile == null)
			return null;
		return new PlayerReference(playerProfile.getId(), playerProfile.getName());
	}
	
	public static PlayerReference of(Entity entity)
	{
		if(entity instanceof Player)
			return of((Player)entity);
		return null;
	}
	
	public static PlayerReference of(Player player)
	{
		return of(player.getGameProfile());
	}
	
	public static PlayerReference of(String playerName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return of(server.getProfileCache().get(playerName).orElse(null));
		return null;
	}
	
	public static boolean listContains(List<PlayerReference> list, UUID id)
	{
		for(PlayerReference player : list)
		{
			if(player.is(id))
				return true;
		}
		return false;
	}
	
	public static boolean listContains(List<PlayerReference> list, String name)
	{
		for(PlayerReference player : list)
		{
			if(player.is(name))
				return true;
		}
		return false;
	}
	
	public static void removeFromList(List<PlayerReference> list, UUID id)
	{
		for(int i = 0; i < list.size(); ++i)
		{
			if(list.get(i).is(id))
			{
				list.remove(i);
				return;
			}
		}
	}
	
	public static void removeFromList(List<PlayerReference> list, String name)
	{
		for(int i = 0; i < list.size(); ++i)
		{
			if(list.get(i).is(name))
			{
				list.remove(i);
				return;
			}
		}
	}
	
	@Override
	public int hashCode() { return this.id.hashCode(); }
	
}

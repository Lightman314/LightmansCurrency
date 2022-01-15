package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PlayerReference {

	public final UUID id;
	private String name = "";
	public String lastKnownName() { return this.name; }
	
	private PlayerReference(UUID playerID, String name)
	{
		this.id = playerID;
		this.name = name;
	}
	
	public void tryUpdateName(PlayerEntity player)
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
		return entity.getUniqueID().equals(this.id);
	}
	
	public boolean is(String name)
	{
		return this.name.toLowerCase().contentEquals(name.toLowerCase());
	}
	
	public CompoundNBT save()
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putUniqueId("id", this.id);
		compound.putString("name", this.name);
		return compound;
	}
	
	public static PlayerReference load(CompoundNBT compound)
	{
		try {
			UUID id = compound.getUniqueId("id");
			String name = compound.getString("name");
			return new PlayerReference(id, name);
		} catch(Exception e) { LightmansCurrency.LogError("Error loading PlayerReference from tag.", e.getMessage()); return null; }
	}
	
	public static void saveList(CompoundNBT compound, List<PlayerReference> playerList, String tag)
	{
		ListNBT list = new ListNBT();
		for(int i = 0; i < playerList.size(); ++i)
		{
			CompoundNBT thisCompound = playerList.get(i).save();
			list.add(thisCompound);
		}
		compound.put(tag, list);
	}
	
	public static List<PlayerReference> loadList(CompoundNBT compound, String tag)
	{
		List<PlayerReference> playerList = Lists.newArrayList();
		ListNBT list = compound.getList(tag, Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundNBT thisCompound = list.getCompound(i);
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
			return of(server.getPlayerProfileCache().getProfileByUUID(playerID));
		return new PlayerReference(playerID, name);
	}
	
	public static PlayerReference of(GameProfile profile)
	{
		if(profile == null)
			return null;
		return new PlayerReference(profile.getId(), profile.getName());
	}
	
	public static PlayerReference of(Entity entity)
	{
		if(entity instanceof PlayerEntity)
			return of((PlayerEntity)entity);
		return null;
	}
	
	public static PlayerReference of(PlayerEntity player)
	{
		return of(player.getGameProfile());
	}
	
	public static PlayerReference of(String playerName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return of(server.getPlayerProfileCache().getGameProfileForUsername(playerName));
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

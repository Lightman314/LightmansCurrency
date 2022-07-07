package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerReference {

	private static Map<UUID,PlayerReference> knownReferences = new HashMap<>();
	public static void clearPlayerCache() { knownReferences.clear(); }
	public static List<PlayerReference> getKnownPlayers() { return knownReferences.values().stream().collect(Collectors.toList()); }
	
	public final UUID id;
	private String name = "";
	public String lastKnownName() { return this.name; }
	public MutableComponent lastKnownNameComponent() { return new TextComponent(this.name); }
	
	private PlayerReference(UUID playerID, String name)
	{
		this.id = playerID;
		this.name = name;
	}
	
	/**
	 * @deprecated No longer needed, as the name is updated every time a reference is created on the server.
	 */
	@Deprecated
	public void tryUpdateName(Player player)
	{
		if(is(player))
			this.name = player.getGameProfile().getName();
	}
	
	/**
	 * Used to run an action/interaction under a team's name.
	 */
	public PlayerReference copyWithName(String name) {
		return new PlayerReference(this.id, name);
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
	
	public boolean isOnline()
	{
		return this.getPlayer() != null;
	}
	
	public Player getPlayer() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.getPlayerList().getPlayer(this.id);
		return null;
	}
	
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		compound.putUUID("id", this.id);
		compound.putString("name", this.name);
		return compound;
	}
	
	public JsonObject saveAsJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", this.id.toString());
		json.addProperty("name", this.name);
		return json;
	}
	
	public static PlayerReference load(CompoundTag compound)
	{
		try {
			UUID id = compound.getUUID("id");
			String name = compound.getString("name");
			return of(id, name);
		} catch(Exception e) { LightmansCurrency.LogError("Error loading PlayerReference from tag.", e.getMessage()); return null; }
	}
	
	public static PlayerReference load(JsonElement json) {
		try {
			if(json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
				return PlayerReference.of(json.getAsString());
			JsonObject j = json.getAsJsonObject();
			UUID id = UUID.fromString(j.get("id").getAsString());
			String name = j.get("name").getAsString();
			return of(id, name);
		} catch(Exception e) {LightmansCurrency.LogError("Error loading PlayerReference from JsonObject", e); return null; }
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
	
	public static PlayerReference of(UUID playerID, String name) { return of(playerID, name, false); }
	
	public static PlayerReference of(UUID playerID, String name, boolean isTrueName)
	{
		if(playerID == null)
			return null;
		//Attempt to get the players profile, for latest name updates
		if(!isTrueName)
		{
			//Only attempt this if this isn't already the true name.
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server != null)
			{
				GameProfile profile = server.getProfileCache().get(playerID).orElse(null);
				if(profile == null || profile.getName() == null)
					return null;
				name = profile.getName();
				isTrueName = true;
			}
		}
		
		//Check if any known references have the same id
		if(knownReferences.containsKey(playerID))
		{
			PlayerReference pr = knownReferences.get(playerID);
			if(isTrueName) //If this is known to be the true player name, update the name
				pr.name = name;
			return pr;
		}
		//Otherwise, create the reference
		PlayerReference newPR = new PlayerReference(playerID, name);
		knownReferences.put(playerID, newPR);
		return newPR;
	}
	
	public static PlayerReference of(GameProfile playerProfile)
	{
		if(playerProfile == null)
			return null;
		return of(playerProfile.getId(), playerProfile.getName(), true);
	}
	
	public static PlayerReference of(Entity entity)
	{
		if(entity instanceof Player)
			return of((Player)entity);
		return null;
	}
	
	public static PlayerReference of(Player player)
	{
		if(player == null)
			return null;
		return of(player.getGameProfile());
	}
	
	public static PlayerReference of(String playerName)
	{
		if(playerName.isBlank())
			return null;
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

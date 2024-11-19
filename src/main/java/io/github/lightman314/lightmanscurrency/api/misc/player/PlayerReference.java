package io.github.lightman314.lightmanscurrency.api.misc.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.util.ItemStackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PlayerReference {

	public static final PlayerReference NULL;

	static {
		NULL = new PlayerReference(new UUID(0,0),"NULL");
		NULL.forceName = true;
	}

	public final UUID id;
	private boolean forceName = false;
	private final String name;
	public String getName(boolean isClient)
	{
		if(this.forceName)
			return this.name;
		if(isClient)
		{
			String n = ClientPlayerNameCache.lookupName(this.id);
			return Objects.requireNonNullElse(n,this.name);
		}
		else
		{
			String n = getPlayerName(this.id);
			if(n == null || n.isBlank())
				return this.name;
			return n;
		}
	}
	public MutableComponent getNameComponent(boolean isClient) { return Component.literal(this.getName(isClient)); }
	public ItemStack getSkull(boolean isClient) { return ItemStackHelper.skullForPlayer(this.getName(isClient)); }

	private PlayerReference(UUID playerID, String name)
	{
		this.id = playerID;
		this.name = name;
	}
	
	/**
	 * Used to run an action/interaction under a team's name.
	 */
	public PlayerReference copyWithName(String name) {
		PlayerReference copy = new PlayerReference(this.id, name);
		copy.forceName = true;
		return copy;
	}
	
	public boolean is(PlayerReference player)
	{
		if(player == null)
			return false;
		return is(player.id);
	}

	public boolean isExact(PlayerReference player)
	{
		if(player == null)
			return false;
		return is(player.id) && !this.forceName && !player.forceName;
	}
	
	public boolean is(GameProfile profile) { return is(profile.getId()); }
	
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
	
	public boolean isOnline() { return this.getPlayer() != null; }
	
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
		compound.putString("name", this.getName(false));
		if(this.forceName)
			compound.putBoolean("forcedname", this.forceName);
		return compound;
	}
	
	public JsonObject saveAsJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", this.id.toString());
		json.addProperty("name", this.getName(false));
		return json;
	}

	public void encode(FriendlyByteBuf buffer, boolean isClient)
	{
		buffer.writeUUID(this.id);
		buffer.writeUtf(this.getName(isClient));
	}
	
	public static PlayerReference load(CompoundTag compound)
	{
		try {
			UUID id = compound.getUUID("id");
			String name = compound.getString("name");
			PlayerReference pr = of(id, name);
			if(compound.contains("forcedname"))
				pr.forceName = compound.getBoolean("forcedname");
			return pr;
		} catch(Exception e) { LightmansCurrency.LogError("Error loading PlayerReference from tag.", e); return null; }
	}
	
	public static PlayerReference load(JsonElement json) {
		try {
			if(json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
				return PlayerReference.of(false, json.getAsString());
			JsonObject j = json.getAsJsonObject();
			UUID id = UUID.fromString(j.get("id").getAsString());
			String name = j.get("name").getAsString();
			return of(id, name);
		} catch(Exception e) {LightmansCurrency.LogError("Error loading PlayerReference from JsonObject", e); return null; }
	}

	public static PlayerReference decode(FriendlyByteBuf buffer) { return of(buffer.readUUID(),buffer.readUtf()); }
	
	public static void saveList(CompoundTag compound, List<PlayerReference> playerList, String tag)
	{
		ListTag list = new ListTag();
		for (PlayerReference playerReference : playerList) {
			if(playerReference != null)
				list.add(playerReference.save());
		}
		compound.put(tag, list);
	}

	public static JsonArray saveJsonList(List<PlayerReference> playerList)
	{
		JsonArray array = new JsonArray();
		for(PlayerReference playerReference : playerList)
		{
			if(playerReference != null)
				array.add(playerReference.saveAsJson());
		}
		return array;
	}
	
	public static List<PlayerReference> loadList(CompoundTag compound, String tag)
	{
		List<PlayerReference> playerList = new ArrayList<>();
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag thisCompound = list.getCompound(i);
				PlayerReference player = load(thisCompound);
				if(player != null)
					playerList.add(player);
			}
		}
		return playerList;
	}
	
	public static PlayerReference of(@Nonnull UUID playerID, String name)
	{
		if(playerID == null)
			throw new RuntimeException("Cannot make a PlayerReference from a null player ID!");
		return new PlayerReference(playerID, name);
	}
	
	public static PlayerReference of(GameProfile playerProfile)
	{
		if(playerProfile == null)
			return null;
		return of(playerProfile.getId(), playerProfile.getName());
	}
	
	public static PlayerReference of(Entity entity)
	{
		if(entity instanceof Player player)
			return of(player);
		return null;
	}
	
	public static PlayerReference of(Player player)
	{
		if(player == null)
			return null;
		return of(player.getGameProfile());
	}
	
	public static PlayerReference of(boolean isClient, String playerName)
	{
		if(playerName.isBlank())
			return null;
		if(isClient)
		{
			UUID id = ClientPlayerNameCache.lookupID(playerName);
			if(id == null)
				return PlayerReference.of(id,playerName);
			//LightmansCurrency.LogWarning("Attempted to assemble a player reference from name alone on a client. Should not be doing that.");
			return null;
		}
		UUID playerID = getPlayerID(playerName);
		if(playerID != null)
			return of(playerID, playerName);
		return null;
	}

	public static boolean isInList(List<PlayerReference> list, Entity entry) { if(entry != null) return isInList(list, entry.getUUID()); return false; }
	
	public static boolean isInList(List<PlayerReference> list, PlayerReference entry) { if(entry != null) return isInList(list, entry.id); return false; }
	
	public static boolean isInList(List<PlayerReference> list, UUID id)
	{
		for(PlayerReference player : list)
		{
			if(player != null && player.is(id))
				return true;
		}
		return false;
	}
	
	public static boolean removeFromList(List<PlayerReference> list, PlayerReference entry) { if(entry != null) return removeFromList(list, entry.id); return false; }
	
	public static boolean removeFromList(List<PlayerReference> list, UUID id)
	{
		for(int i = 0; i < list.size(); ++i)
		{
			PlayerReference pr = list.get(i);
			if(pr != null && pr.is(id))
			{
				list.remove(i);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() { return this.id.hashCode(); }
	
	/**
	 * Only run on server.
	 */
	public static String getPlayerName(UUID playerID)
	{
		try {
			String name = UsernameCache.getLastKnownUsername(playerID);
			if(name != null)
				return name;
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server != null)
			{
				GameProfile profile = server.getProfileCache().get(playerID).orElse(null);
				if(profile != null)
					return profile.getName();
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error getting player name.", t); }
		return null;
	}
	
	/**
	 * Only run on server.
	 */
	public static UUID getPlayerID(String playerName)
	{
		try {
			for(Entry<UUID,String> entry : UsernameCache.getMap().entrySet())
			{
				if(entry.getValue().equalsIgnoreCase(playerName))
					return entry.getKey();
			}
			
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server != null)
			{
				GameProfile profile = server.getProfileCache().get(playerName).orElse(null);
				if(profile != null)
					return profile.getId();
			}
			
		} catch(Throwable t) { LightmansCurrency.LogError("Error getting player ID from name.", t); }
		return null;
	}
	
}

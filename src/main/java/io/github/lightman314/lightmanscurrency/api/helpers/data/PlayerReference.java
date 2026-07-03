package io.github.lightman314.lightmanscurrency.api.helpers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public class PlayerReference {

    public static final Codec<PlayerReference> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(pr -> pr.id),
                    Codec.STRING.fieldOf("name").forGetter(pr -> pr.getName(ISidedContext.LOGICAL_SERVER)),
                    Codec.BOOL.fieldOf("forcedname").forGetter(pr -> pr.forceName)
            ).apply(builder,PlayerReference::new));

    public static final Codec<List<PlayerReference>> LIST_CODEC = CODEC.listOf().validate(list -> {
        list = new ArrayList<>(list);
        //Remove Duplicate Values
        for(int i = 0; i < list.size(); ++i)
        {
            PlayerReference pr = list.get(i);
            for(int a = i + 1; a < list.size(); ++a)
            {
                if(pr.equals(list.get(a)))
                {
                    list.remove(a);
                    a--;
                }
            }
        }
        return DataResult.success(list);
    });

    public static final StreamCodec<FriendlyByteBuf,PlayerReference> STREAM_CODEC = StreamCodec.of((buf,pr) -> { buf.writeUUID(pr.id); buf.writeUtf(pr.name); buf.writeBoolean(pr.forceName); },buf -> new PlayerReference(buf.readUUID(),buf.readUtf(),buf.readBoolean()));
    public static final StreamCodec<FriendlyByteBuf,List<PlayerReference>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final PlayerReference NULL = new PlayerReference(new UUID(0,0),"NULL",true);

    public final UUID id;
    private boolean forceName;
    private final String name;
    public String getName(boolean isClient) { return this.getName(ISidedContext.known(isClient)); }
    public String getName(ISidedContext context)
    {
        if(this.forceName)
            return this.name;
        if(context.isClient())
        {
            //TODO re-implement the client player name cache
            //String n = ClientPlayerNameCache.lookupName(this.id);
            //return Objects.requireNonNullElse(n,this.name);
            return this.name;
        }
        else
        {
            String n = getPlayerName(this.id);
            if(n == null || n.isBlank())
                return this.name;
            return n;
        }
    }
    public Component getNameComponent(boolean isClient) { return this.getNameComponent(ISidedContext.known(isClient)); }
    public Component getNameComponent(ISidedContext context) { return Component.literal(this.getName(context)); }
    public ItemStack getSkull() { return ItemHelper.skullForPlayer(this.id); }

    private PlayerReference(UUID playerID, String name, boolean forceName) { this.id = playerID; this.name = name; this.forceName = forceName; }
    private PlayerReference(UUID playerID, String name) { this(playerID,name,false); }

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

    public boolean is(GameProfile profile) { return is(profile.id()); }

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

    public static PlayerReference of(UUID playerID, String name)
    {
        if(playerID == null)
            throw new RuntimeException("Cannot make a PlayerReference from a null player ID!");
        return new PlayerReference(playerID, name);
    }

    public static PlayerReference of(GameProfile playerProfile)
    {
        if(playerProfile == null)
            return null;
        return of(playerProfile.id(),playerProfile.name());
    }

    @Nullable
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
            /*UUID id = ClientPlayerNameCache.lookupID(playerName);
            if(id != null)
                return PlayerReference.of(id,playerName);*/
            //LightmansCurrency.LogWarning("Attempted to assemble a player reference from name alone on a client. Should not be doing that.");
            return null;
        }
        UUID playerID = getPlayerID(playerName);
        if(playerID != null)
            return of(playerID, playerName);
        return null;
    }

    public static PlayerReference dummy(String dummyName) {
        PlayerReference pr = new PlayerReference(new UUID(0,0),dummyName);
        pr.forceName = true;
        return pr;
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

    public static boolean addToList(List<PlayerReference> list, PlayerReference entry)
    {
        if(!isInList(list,entry))
        {
            list.add(entry);
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
    public int hashCode() {
        if(this.forceName)
            return Objects.hash(this.id,this.name);
        return this.id.hashCode();
    }

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
                GameProfile profile = server.services().profileResolver().fetchById(playerID).orElse(null);
                if(profile != null)
                    return profile.name();
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
                GameProfile profile = server.services().profileResolver().fetchByName(playerName).orElse(null);
                if(profile != null)
                    return profile.id();
            }

        } catch(Throwable t) { LightmansCurrency.LogError("Error getting player ID from name.", t); }
        return null;
    }

    @Override
    public String toString() { return "PlayerReference[" + this.id + ";" + this.name + "]"; }

}
package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.AllySettings;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.PermissionSettings;

import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveAllyNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeAllyPermissionNotification;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AlliesNode extends SyncedTraderNode implements IPermissionProvider {

    private static final MapCodec<AlliesNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PlayerReference.CODEC.listOf().fieldOf("allies").forGetter(n -> n.allies),
            Codec.unboundedMap(Codec.STRING,Codec.INT).fieldOf("allyPermissions").forGetter(n -> n.allyPermissions)
    ).apply(builder,AlliesNode::new));

    public static final TraderNodeType<AlliesNode> TYPE = TraderNodeType.simple(AlliesNode::new,MAP_CODEC);

    private final List<PlayerReference> allies;
    public List<PlayerReference> getAllies() { return ImmutableList.copyOf(this.allies); }
    public void setAllies(List<PlayerReference> allies) {
        this.allies.clear();
        this.allies.addAll(allies);
        this.setAlliesChanged();
    }
    public boolean addAlly(@Nullable PlayerReference admin, PlayerReference newAlly)
    {
        if(!PlayerReference.isInList(this.allies,newAlly))
        {
            this.allies.add(newAlly);
            this.setAlliesChanged();
            if(admin != null)
                this.pushLocalNotification(new AddRemoveAllyNotification(admin, true, newAlly));
            return true;
        }
        return false;
    }
    public boolean removeAlly(@Nullable PlayerReference admin, PlayerReference oldAlly)
    {
        if(PlayerReference.removeFromList(this.allies, oldAlly))
        {
            this.setAlliesChanged();
            if(admin != null)
                this.pushLocalNotification(new AddRemoveAllyNotification(admin, false, oldAlly));
            return true;
        }
        return false;
    }
    private final Map<String,Integer> allyPermissions;
    public Map<String,Integer> getAllyPermissionsMap() { return ImmutableMap.copyOf(this.allyPermissions); }
    public void setAllyPermissions(Map<String,Integer> newMap)
    {
        for(String key : new ArrayList<>(this.allyPermissions.keySet()))
            this.allyPermissions.put(key,0);
        this.allyPermissions.putAll(newMap);
        this.onAttach();
        this.setPermissionsChanged();
    }
    public boolean setAllyPermission(@Nullable PlayerReference admin, String permission, int newLevel)
    {
        int oldLevel = this.allyPermissions.getOrDefault(permission,0);
        if(oldLevel != newLevel)
        {
            this.allyPermissions.put(permission,newLevel);
            this.setPermissionsChanged();
            if(admin != null)
                this.pushLocalNotification(new ChangeAllyPermissionNotification(admin,permission,newLevel,oldLevel));
            return true;
        }
        return false;
    }

    private void setAlliesChanged()
    {
        this.setChanged(builder ->
            builder.setList("allies",this.allies, ModLazyPackets.PLAYER_REFERENCE)
        );
    }
    private void setPermissionsChanged()
    {
        this.setChanged(builder -> {
            LazyPacketData.Builder map = this.builder();
            this.allyPermissions.forEach(map::setInt);
            builder.setMap("permissions",map);
        });
    }

    private AlliesNode() { this(new ArrayList<>(),new HashMap<>()); }
    private AlliesNode(List<PlayerReference> allies, Map<String,Integer> allyPermissions)
    {
        this.allies = allies;
        this.allyPermissions = allyPermissions;
    }

    @Override
    public void onAttach() {
        this.trader.initializeAllyPermissions(this::initializePermission);
        for(TraderNode node : this.trader.getNodeIterable())
            node.initializeAllyPermissions(this::initializePermission);
    }

    private void initializePermission(String key, int level) {
        if(!this.allyPermissions.containsKey(key))
            this.allyPermissions.put(key,level);
    }

    @Override
    public int getPermissionLevel(String permission,@Nullable PlayerReference player) {
        if(PlayerReference.isInList(this.allies,player))
            return this.allyPermissions.getOrDefault(permission,0);
        return 0;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setList("allies",this.allies,ModLazyPackets.PLAYER_REFERENCE);
        LazyPacketData.Builder permBuilder = this.builder();
        this.allyPermissions.forEach(permBuilder::setInt);
        builder.setMap("permissions",permBuilder);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("allies"))
        {
            this.allies.clear();
            this.allies.addAll(data.getList("allies",ModLazyPackets.PLAYER_REFERENCE));
        }
        if(data.contains("permissions"))
        {
            this.allyPermissions.clear();
            LazyPacketData map = data.getMap("permissions");
            for(String key : map.keySet())
                this.allyPermissions.put(key,map.getInt(key));
        }
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.ADD_REMOVE_ALLIES,0);
        defaultConsumer.accept(Permissions.EDIT_PERMISSIONS, 0);
    }

    @Override
    public boolean blockPermission(String permission) {
        if(this.trader instanceof IPersistentTrader pt && pt.isPersistent() && (Permissions.ADD_REMOVE_ALLIES.equals(permission) || Permissions.EDIT_PERMISSIONS.equals(permission)))
            return true;
        return super.blockPermission(permission);
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("ChangeAllyPermissions"))
        {
            if(this.hasPermission(player, Permissions.EDIT_PERMISSIONS))
            {
                String permission = message.getString("ChangeAllyPermissions");
                int newLevel = message.getInt("NewLevel");
                this.setAllyPermission(PlayerReference.of(player),permission,newLevel);
            }
        }
        if(message.contains("AddAlly"))
        {
            if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
            {
                PlayerReference newAlly = message.getCustom("AddAlly",ModLazyPackets.PLAYER_REFERENCE);
                if(newAlly != null)
                    this.addAlly(PlayerReference.of(player),newAlly);
            }
        }
        if(message.contains("RemoveAlly"))
        {
            if(this.hasPermission(player, Permissions.ADD_REMOVE_ALLIES))
            {
                PlayerReference oldAlly = message.getCustom("RemoveAlly",ModLazyPackets.PLAYER_REFERENCE);
                if(oldAlly != null)
                    this.removeAlly(PlayerReference.of(player),oldAlly);
            }
        }
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new AllySettings(trader,this));
        consumer.accept(new PermissionSettings(trader,this));
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Allies"))
        {
            this.allies.clear();
            this.allies.addAll(PlayerReference.loadList(tag, "Allies"));
        }
        if(tag.contains("AllyPermissions"))
        {
            this.allyPermissions.clear();
            ListTag allyPermList = tag.getList("AllyPermissions", Tag.TAG_COMPOUND);
            for(int i = 0; i < allyPermList.size(); ++i)
            {
                CompoundTag entry = allyPermList.getCompound(i);
                String perm = entry.getString("Permission");
                int level = entry.getInt("Level");
                this.allyPermissions.put(perm,level);
            }
        }
    }

}

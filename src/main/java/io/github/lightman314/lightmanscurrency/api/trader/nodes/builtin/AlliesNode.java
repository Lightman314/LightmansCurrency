package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReferenceList;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeAllyNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeAllyPermissionNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class AlliesNode extends SimpleSyncedNode implements IPermissionSource, IPermissionUser, ISettingsMessageListener, ISettingsStorageIONode.PriorityLoad {

    private static final MapCodec<AlliesNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PlayerReferenceList.CODEC.fieldOf("allies").forGetter(n -> n.allies),
            Permission.DATA_CODEC.fieldOf("permissions").forGetter(AlliesNode::getPermissionData)
    ).apply(builder,AlliesNode::new));
    public static final TraderNodeType<AlliesNode> TYPE = TraderNodeType.simple(AlliesNode::new,CODEC);

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_ALLIES = TextEntry.traderNodeValue(TYPE,"allies");
    public static final TextEntry VALUE_ALLY_PERMS = TextEntry.traderNodeValue(TYPE,"ally_permissions");

    private final PlayerReferenceList allies = new PlayerReferenceList("allies","allyChanges").withListener(this::setChanged);
    public List<PlayerReference> getAllies() { return this.allies.getPlayers(); }
    public boolean addAlly(PlayerReference newAlly,@Nullable PlayerReference admin) {
        if(this.allies.add(newAlly)) {
            if(admin != null)
                this.postInternalNotification(ChangeAllyNotification.added(admin,newAlly));
            return true;
        }
        return false;
    }
    public boolean removeAlly(PlayerReference oldAlly,@Nullable PlayerReference admin) {
        if(this.allies.remove(oldAlly)) {
            if(admin != null)
                this.postInternalNotification(ChangeAllyNotification.removed(admin,oldAlly));
            return true;
        }
        return false;
    }

    private final Map<Permission<?>,PermissionValue<?>> permissions;
    public final Map<Permission<?>,PermissionValue<?>> getPermissionData() { return ImmutableMap.copyOf(this.permissions); }
    public boolean setAllyPermission(Permission<?> permission,Object newValue,@Nullable PlayerReference admin) {
        if(this.permissions.containsKey(permission)) {
            PermissionValue<?> value = this.permissions.get(permission);
            if(value.setUnsafe(newValue)) {
                this.setChanged(p -> p.modifyMap("changePermission",e ->
                    e.set(value.getKey(),LCFancyPacketTypes.PERMISSION_VALUE,value)
                ));
                if(admin != null)
                    this.postInternalNotification(ChangeAllyPermissionNotification.of(admin,value));
                return true;
            }
        }
        return false;
    }

    private AlliesNode() { this.permissions = new HashMap<>(); }
    private AlliesNode(PlayerReferenceList allies, Map<Permission<?>,PermissionValue<?>> permissions)
    {
        this.allies.copyFrom(allies,false);
        this.permissions = new HashMap<>(permissions);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public <T> T getPlayerPermission(PlayerReference player,Permission<T> permission) {
        if(this.allies.isInList(player))
            return this.getAllyPermissionValue(permission);
        return permission.getEmpty();
    }

    public final <T> T getAllyPermissionValue(Permission<T> permission)
    {
        PermissionValue<T> value = (PermissionValue<T>)this.permissions.get(permission);
        if(value != null)
            return value.get();
        return permission.getEmpty();
    }

    //Collect the permission options from the other nodes
    @Override
    public void onAttach() {
        this.validatePermissionsMap();
    }

    private void validatePermissionsMap() {
        if(this.isServer())
        {
            Map<Permission<?>,PermissionValue<?>> newMap = new HashMap<>();
            for(IPermissionUser node : this.getNodes(IPermissionUser.class))
            {
                node.addDefaultAllyPermission(perm -> {
                    if(!newMap.containsKey(perm))
                    {
                        //attempt to get the value from the current map
                        if(this.permissions.containsKey(perm))
                            newMap.put(perm,this.permissions.get(perm));
                        else //Otherwise create a new value
                            newMap.put(perm,perm.createNew());
                    }
                });
            }
            this.permissions.clear();
            this.permissions.putAll(newMap);
        }
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        this.allies.writePacket(builder);
        FancyPacketMap.Mutable permMap = FancyPacketMap.map();
        for(PermissionValue<?> perm : new HashSet<>(this.permissions.values()))
            permMap.set(perm.getKey(),LCFancyPacketTypes.PERMISSION_VALUE,perm);
        builder.setMap("permissions",permMap);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        //Allies
        this.allies.handlePacket(data);
        //Permissions
        if(data.contains("permissions"))
        {
            this.permissions.clear();
            FancyPacketMap map = data.getMap("permissions");
            for(String key : map.keySet())
            {
                PermissionValue<?> value = map.get(key,LCFancyPacketTypes.PERMISSION_VALUE);
                if(value != null)
                    this.permissions.put(value.getPerm(),value);
            }
        }
        if(data.contains("changePermission"))
        {
            FancyPacketMap entry = data.getMap("changePermission");
            for(String key : entry.keySet())
            {
                PermissionValue<?> value = entry.get(key,LCFancyPacketTypes.PERMISSION_VALUE);
                if(value != null)
                    this.permissions.put(value.getPerm(),value);
            }
        }
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(LCPermissions.ADD_REMOVE_ALLIES);
        handler.accept(LCPermissions.EDIT_ALLY_PERMS);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.store("allies", PlayerReferenceList.CODEC,this.allies);
        output.store("permissions",Permission.DATA_CODEC,this.permissions);
    }

    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext.Mutable context) {
        if(context.getPermission(LCPermissions.ADD_REMOVE_ALLIES)) {
            //Inform the context about the previous allies list
            context.definePreviousPermissionMapMembers(this.allies.getPlayers());
            this.allies.copyFrom(data.read("allies", PlayerReferenceList.CODEC).orElse(new PlayerReferenceList()),true);
        }
        if(context.getPermission(LCPermissions.EDIT_ALLY_PERMS)) {
            //Inform the context about the previous ally map
            context.definePreviousPermissionMap(Map.copyOf(this.permissions));
            //Now load the permission map
            this.permissions.clear();
            this.permissions.putAll(data.read("permissions",Permission.DATA_CODEC).orElse(Map.of()));
            //Now re-validate the permissions map contents as though this was a trader attachment
            this.validatePermissionsMap();
            this.setChanged(w -> {
                FancyPacketMap.Mutable permMap = FancyPacketMap.map();
                for(PermissionValue<?> perm : new HashSet<>(this.permissions.values()))
                    permMap.set(perm.getKey(),LCFancyPacketTypes.PERMISSION_VALUE,perm);
                w.setMap("permissions",permMap);
            });
        }
    }

    @Override
    public void appendDisplay(ValueInput data,SettingsDisplayOutput output) {
        output.acceptTitle(NAME);
        output.acceptEntry(VALUE_ALLIES,data.read("allies", PlayerReferenceList.CODEC).orElse(new PlayerReferenceList()).size());
        output.acceptEntry(VALUE_ALLY_PERMS,data.read("permissions",Permission.DATA_CODEC).orElse(Map.of()).size());
    }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {
        if(message.contains("addAlly") && this.getPermission(player,LCPermissions.ADD_REMOVE_ALLIES))
            this.addAlly(message.get("addAlly",LCFancyPacketTypes.PLAYER_REFERENCE),PlayerReference.of(player));
        if(message.contains("removeAlly") && this.getPermission(player,LCPermissions.ADD_REMOVE_ALLIES))
            this.removeAlly(message.get("removeAlly",LCFancyPacketTypes.PLAYER_REFERENCE),PlayerReference.of(player));
        if(message.contains("changePermission") && this.getPermission(player,LCPermissions.EDIT_ALLY_PERMS)) {
            PermissionValue<?> newValue = message.get("changePermission",LCFancyPacketTypes.PERMISSION_VALUE);
            if(newValue != null)
                this.setAllyPermission(newValue.getPerm(),newValue.get(),PlayerReference.of(player));
        }
    }

}
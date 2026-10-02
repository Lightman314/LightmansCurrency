package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;
import java.util.function.Consumer;

public class AlliesNode extends SimpleSyncedNode implements IPermissionSource, IPermissionUser, ISettingsStorageIONode.PriorityLoad {

    private static final MapCodec<AlliesNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PlayerReference.LIST_CODEC.fieldOf("allies").forGetter(AlliesNode::getAllies),
            Permission.DATA_CODEC.fieldOf("permissions").forGetter(AlliesNode::getPermissionData)
    ).apply(builder,AlliesNode::new));
    public static final TraderNodeType<AlliesNode> TYPE = TraderNodeType.simple(AlliesNode::new,CODEC);

    private final List<PlayerReference> allies;
    public List<PlayerReference> getAllies() { return ImmutableList.copyOf(this.allies); }
    private final Map<Permission<?>,PermissionValue<?>> permissions;
    public final Map<Permission<?>,PermissionValue<?>> getPermissionData() { return ImmutableMap.copyOf(this.permissions); }

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_ALLIES = TextEntry.traderNodeValue(TYPE,"allies");
    public static final TextEntry VALUE_ALLY_PERMS = TextEntry.traderNodeValue(TYPE,"ally_permissions");

    private AlliesNode() { this(ImmutableList.of(),ImmutableMap.of()); }
    private AlliesNode(List<PlayerReference> allies,Map<Permission<?>,PermissionValue<?>> permissions)
    {
        this.allies = new ArrayList<>(allies);
        this.permissions = new HashMap<>(permissions);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public <T> T getPlayerPermission(PlayerReference player,Permission<T> permission) {
        if(PlayerReference.isInList(this.allies,player))
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

    public void setAllyPermissionValue(PermissionValue<?> newValue) {
        if(this.permissions.containsKey(newValue.getPerm()))
        {
            PermissionValue<?> oldValue = this.permissions.get(newValue.getPerm());
            if(!oldValue.get().equals(newValue.get()))
            {
                this.permissions.put(newValue.getPerm(),newValue);
                this.setChanged(builder -> builder.modifyMap("changePermission",map -> map.set(newValue.getKey(),LCFancyPacketTypes.PERMISSION_VALUE,newValue)));
            }
        }
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
        builder.setList("allies", LCFancyPacketTypes.PLAYER_REFERENCE,this.allies);
        FancyPacketMap.Mutable permMap = FancyPacketMap.map();
        for(PermissionValue<?> perm : new HashSet<>(this.permissions.values()))
            permMap.set(perm.getKey(),LCFancyPacketTypes.PERMISSION_VALUE,perm);
        builder.setMap("permissions",permMap);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        //Allies
        if(data.contains("allies"))
        {
            this.allies.clear();
            this.allies.addAll(data.getList("allies", LCFancyPacketTypes.PLAYER_REFERENCE));
        }
        if(data.contains("addAlly"))
            PlayerReference.addToList(this.allies,data.get("addAlly", LCFancyPacketTypes.PLAYER_REFERENCE));
        if(data.contains("removeAlly"))
            PlayerReference.removeFromList(this.allies,data.get("removeAlly", LCFancyPacketTypes.PLAYER_REFERENCE));
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
        output.store("allies",PlayerReference.LIST_CODEC,this.allies);
        output.store("permissions",Permission.DATA_CODEC,this.permissions);
    }

    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext.Mutable context) {
        if(context.getPermission(LCPermissions.ADD_REMOVE_ALLIES)) {
            //Inform the context about the previous allies list
            context.definePreviousPermissionMapMembers(List.copyOf(this.allies));
            this.allies.clear();
            this.allies.addAll(data.read("allies",PlayerReference.LIST_CODEC).orElse(List.of()));
            this.setChanged(w -> w.setList("allies",LCFancyPacketTypes.PLAYER_REFERENCE,this.allies));
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
        output.acceptEntry(VALUE_ALLIES,data.read("allies",PlayerReference.LIST_CODEC).orElse(List.of()).size());
        output.acceptEntry(VALUE_ALLY_PERMS,data.read("permissions",Permission.DATA_CODEC).orElse(Map.of()).size());
    }

}

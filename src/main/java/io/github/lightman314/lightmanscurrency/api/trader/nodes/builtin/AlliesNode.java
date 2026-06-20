package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.Consumer;

public class AlliesNode extends SimpleSyncedNode implements IPermissionSource, IPermissionUser {

    private static final MapCodec<AlliesNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PlayerReference.LIST_CODEC.fieldOf("allies").forGetter(AlliesNode::getAllies),
            Permission.DATA_CODEC.fieldOf("permissions").forGetter(AlliesNode::getPermissionData)
    ).apply(builder,AlliesNode::new));

    public static final TraderNodeType<AlliesNode> TYPE = TraderNodeType.simple(AlliesNode::new,CODEC);

    private final List<PlayerReference> allies;
    public List<PlayerReference> getAllies() { return ImmutableList.copyOf(this.allies); }
    private final Map<Permission<?>,PermissionValue<?>> permissions;
    public final Map<Permission<?>,PermissionValue<?>> getPermissionData() { return ImmutableMap.copyOf(this.permissions); }

    private AlliesNode() { this(ImmutableList.of(),ImmutableMap.of()); }
    private AlliesNode(List<PlayerReference> allies,Map<Permission<?>, PermissionValue<?>> permissions)
    {
        this.allies = new ArrayList<>(allies);
        this.permissions = new HashMap<>(permissions);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public <T> T getPlayerPermission(PlayerReference player, Permission<T> permission) {
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

    //Collect the permission options from the other nodes
    @Override
    public void onAttach() {
        if(this.isServer())
        {
            Map<Permission<?>,PermissionValue<?>> newMap = new HashMap<>();
            for(IPermissionUser node : this.trader.getNodes(IPermissionUser.class))
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
        FancyPacketMap.Mutable permMap = FancyPacketMap.newMutable();
        for(Permission<?> perm : new HashSet<>(this.permissions.keySet()))
            permMap.set(perm.getKey().toString(), LCFancyPacketTypes.PERMISSION_VALUE,this.permissions.get(perm));
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
                try{
                    Permission<?> perm = LCRegistries.Trader.PERMISSION.getValue(Identifier.parse(key));
                    if(perm == null)
                        continue;
                    PermissionValue<?> value = map.get(key, LCFancyPacketTypes.PERMISSION_VALUE);
                    if(value != null && value.is(perm))
                        this.permissions.put(perm,value);
                } catch (IdentifierException ignored) {}
            }
        }
        if(data.contains("changePermission"))
        {
            FancyPacketMap entry = data.getMap("changePermission");
            Permission<?> perm = entry.get("type", LCFancyPacketTypes.PERMISSION);
            PermissionValue<?> value = entry.get("value", LCFancyPacketTypes.PERMISSION_VALUE);
            if(value != null && value.is(perm))
                this.permissions.put(perm,value);
        }
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(BuiltInPermissions.ADD_REMOVE_ALLIES);
        handler.accept(BuiltInPermissions.EDIT_ALLY_PERMS);
    }

}

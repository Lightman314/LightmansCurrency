package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.OwnerSettings;

import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeOwnerNotification;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OwnerNode extends SyncedTraderNode implements IPermissionProvider, INotificationConsumer, IPersistentNode, IOwnerSource {

    private static final MapCodec<OwnerNode> MAP_CODEC = OwnerData.CODEC.xmap(OwnerNode::new,OwnerNode::getOwner).fieldOf("owner");

    public static final TraderNodeType<OwnerNode> TYPE = TraderNodeType.simple(OwnerNode::new,MAP_CODEC);

    private final OwnerData owner = new OwnerData(this,this::setOwnerChanged);
    public OwnerData getOwner() { return this.owner; }

    private OwnerNode() {}
    private OwnerNode(OwnerData owner) { this.owner.copyFrom(owner); }

    public void setOwnerChanged()
    {
        this.setChanged(builder -> builder.setCustom("owner",this.owner,ModLazyPackets.OWNER_DATA));
    }

    @Override
    @Nullable
    public OwnerData getValidOwner() {
        if(!this.owner.getValidOwner().isNull())
            return this.owner;
        return null;
    }

    @Override
    public int getPermissionLevel(String permission,PlayerReference player) {
        //If they are the owner, they get max level permissions
        if(this.owner.isAdmin(player))
            return Integer.MAX_VALUE;
        //If they are only a member of the owners team, they get ally permissions
        if(this.owner.isMember(player))
        {
            //Grant Ally Permissions
            return this.trader.findNodeValue(AlliesNode.TYPE,AlliesNode::getAllyPermissionsMap,new HashMap<String,Integer>()).getOrDefault(permission,0);
        }
        return 0;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setCustom("owner",this.owner,ModLazyPackets.OWNER_DATA);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("owner"))
            this.owner.copyFrom(data.getCustom("owner",ModLazyPackets.OWNER_DATA));
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.TRANSFER_OWNERSHIP,0);
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        //Core settings
        consumer.accept(new OwnerSettings(trader,this));
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {

        if(message.contains("ChangePlayerOwner"))
        {
            if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
            {
                PlayerReference newOwnerPlayer = PlayerReference.of(this.isClient(),message.getString("ChangePlayerOwner"));
                if(newOwnerPlayer != null)
                {
                    Owner newOwner = PlayerOwner.of(newOwnerPlayer);
                    Owner oldOwner = this.getOwner().getValidOwner();
                    if(oldOwner.matches(newOwner))
                    {
                        LightmansCurrency.LogDebug("Set owner player to the same player who already owns this machine.");
                        return;
                    }
                    this.owner.SetOwner(newOwner);
                    for(TraderNode node : this.trader.getNodeIterable())
                    {
                        if(node instanceof IOwnerListener listener)
                            listener.onOwnerChanged();
                    }
                    //Send Notification
                    this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player), newOwner, oldOwner));
                }
            }
        }
        if(message.contains("ChangeOwner"))
        {
            if(this.hasPermission(player, Permissions.TRANSFER_OWNERSHIP))
            {
                Owner newOwner = message.getOwner("ChangeOwner");
                Owner oldOwner = this.owner.getValidOwner();
                if(newOwner != null && !oldOwner.matches(newOwner))
                {
                    this.owner.SetOwner(newOwner);
                    for(TraderNode node : this.trader.getNodeIterable())
                    {
                        if(node instanceof IOwnerListener listener)
                            listener.onOwnerChanged();
                    }
                    //Send Notification
                    this.pushLocalNotification(new ChangeOwnerNotification(PlayerReference.of(player),newOwner,oldOwner));
                }
            }
        }
    }

    @Override
    public void pushNotification(Supplier<Notification> source, int notificationLevel, boolean notificationsToChat) {
        this.owner.getValidOwner().pushNotification(source,notificationLevel,notificationsToChat);
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("OwnerData", Tag.TAG_COMPOUND))
            this.owner.load(tag.getCompound("OwnerData"),DataContext.createNBT(lookup));
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        json.addProperty("OwnerName",ownerName);
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException{
        Owner owner = FakeOwner.of("Server");
        if(json.has("OwnerName"))
            owner = FakeOwner.of(ComponentSerialization.CODEC.decode(context.ops(),json.get("OwnerName")).getOrThrow(JsonSyntaxException::new).getFirst());
        this.owner.SetOwner(owner);
    }

}

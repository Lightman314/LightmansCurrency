package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatListener;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class OwnerNode extends SimpleSyncedNode implements IOwnerSource,IPermissionSource,INotificationConsumerNode,ISettingsMessageListener,ISettingsStorageIONode.PriorityLoad,IStatListeningNode {

    private static final MapCodec<OwnerNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            OwnerHolder.CODEC.fieldOf("owner").forGetter(OwnerNode::getOwner)
    ).apply(builder,OwnerNode::new));

    public static final TraderNodeType<OwnerNode> TYPE = TraderNodeType.simple(OwnerNode::new,CODEC);

    public static final TextEntry SETTINGS_TOOLTIP = TextEntry.tooltip(LCApi.MODID,"trader.settings.owner");

    public static final TextEntry BUTTON_OWNER_SET_PLAYER = TextEntry.button(LCApi.MODID,"ownership.set_player");
    public static final TextEntry BUTTON_OWNER_SET_FAKEPLAYER = TextEntry.button(LCApi.MODID,"ownership.set_fake_player");
    public static final TextEntry GUI_OWNER_CURRENT = TextEntry.gui(LCApi.MODID,"owner.current");
    public static final TextEntry TOOLTIP_OWNER_NODE_SELECTION = TextEntry.tooltip(LCApi.MODID,"owner_mode.selection");
    public static final TextEntry TOOLTIP_OWNER_NODE_MANUAL = TextEntry.tooltip(LCApi.MODID,"owner_mode.manual");

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_OWNER = TextEntry.traderNodeValue(TYPE,"owner");

    private final OwnerHolder owner = new OwnerHolder(this,this::onOwnerChanged);
    public OwnerHolder getOwner() { return this.owner; }

    private OwnerNode() {}
    private OwnerNode(OwnerHolder owner) { this.owner.copyFrom(owner); }

    @Override
    public void updateArgument(TraderArguments arguments) {
        Optional<Owner> arg = arguments.tryGet(TYPE,Owner.class);
        if(arg.isPresent())
            this.owner.setOwner(arg.get());
    }

    private void onOwnerChanged() {
        this.setChanged(packet -> packet.set("owner", LCFancyPacketTypes.OWNER_HOLDER,this.owner));
        if(this.isServer())
        {
            for(IOwnerListener listener : this.getNodes(IOwnerListener.class))
                listener.onOwnerChanged();
        }
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("owner"))
            this.owner.copyFrom(data.get("owner",LCFancyPacketTypes.OWNER_HOLDER,this.owner));
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.set("owner",LCFancyPacketTypes.OWNER_HOLDER,this.owner);
    }

    @Override
    public Optional<IOwnerHolder> getValidOwner() { return Optional.of(this.owner); }

    @Override
    public <T> T getPlayerPermission(PlayerReference player, Permission<T> permission) {
        if(this.owner.isAdmin(player))
            return permission.getMaxValue();
        //Get ally permissions if member
        if(this.owner.isMember(player))
            return this.getAllyPermission(permission);
        return permission.getEmpty();
    }

    private <T> T getAllyPermission(Permission<T> permission)
    {
        AlliesNode node = this.getNode(AlliesNode.TYPE);
        if(node != null)
            return node.getAllyPermissionValue(permission);
        return permission.getEmpty();
    }

    @Override
    public void processNotification(Notification notification, boolean sendToMembers, MemberLevel targets, boolean pushToChat) {
        if(sendToMembers)
            this.owner.getValidOwner().postNotification(notification,targets,pushToChat);
    }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {
        if(message.contains("setOwner") && this.getPermission(player,LCPermissions.TRANSFER_OWNERSHIP)) {
            Owner newOwner = message.get("setOwner",LCFancyPacketTypes.OWNER);
            this.setOwnerAndLogChange(player,newOwner);
        }
        if(message.contains("setPlayerOwner") && this.getPermission(player,LCPermissions.TRANSFER_OWNERSHIP)) {
            PlayerReference pr = PlayerReference.of(this,message.getString("setPlayerOwner"));
            if(pr != null)
                this.setOwnerAndLogChange(player,PlayerOwner.of(pr));
        }
        //Require admin mode for fake player ownership
        if(message.contains("setFakePlayerOwner") && LCApi.isInAdminMode(player)) {
            this.setOwnerAndLogChange(player,FakeOwner.of(message.getString("setFakePlayerOwner")));
        }
    }

    private void setOwnerAndLogChange(Player player,Owner newOwner) {
        Owner oldOwner = this.owner.getValidOwner();
        if(newOwner != null && !newOwner.equals(oldOwner)) {
            this.owner.setOwner(newOwner);
            this.postInternalNotification(ChangeSettingNotification.advanced(PlayerReference.of(player),VALUE_OWNER.get(),newOwner.getName(),oldOwner.getName()));
        }
    }

    @Override
    public int getSettingsLoadPriority() { return -1000; }
    @Override
    public void encodeSettings(ValueOutput output) {
        output.store("owner",OwnerHolder.CODEC,this.owner);
    }
    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext.Mutable context) {
        if(context.getPermission(LCPermissions.TRANSFER_OWNERSHIP)) {
            OwnerHolder newOwner = data.read("owner",OwnerHolder.CODEC).orElse(new OwnerHolder());
            //Inform the loading context about this previous owner
            context.definePreviousOwner(this.owner.getValidOwner());
            //Now load from the context and flag as changed
            this.owner.copyFrom(newOwner);
            this.onOwnerChanged();
        }
    }

    @Override
    public void appendDisplay(ValueInput data,SettingsDisplayOutput output) {
        output.acceptTitle(NAME);
        output.acceptEntry(VALUE_OWNER,data.read("owner",OwnerHolder.CODEC).orElse(new OwnerHolder()).setSidedContext(this).getName());
    }

    @Override
    public <V, A> void afterStatAdded(StatKey<V, A> key,A addValue) {
        StatListener ownerStats = this.owner.getValidOwner().getStatistics();
        if(ownerStats != null)
            ownerStats.addToStat(key,addValue);
    }

}
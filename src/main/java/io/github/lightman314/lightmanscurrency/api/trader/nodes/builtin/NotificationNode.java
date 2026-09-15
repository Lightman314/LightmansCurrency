package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationFilter;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INotificationConsumerNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INotificationSettingsSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderSettingsCategory;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.InfoTab;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

public class NotificationNode extends SimpleSyncedNode implements INotificationConsumerNode,INotificationSettingsSource,ISettingsMessageListener,IStorageMenuTabProvider {

    private static final MapCodec<NotificationNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NotificationHolder.CODEC.fieldOf("notifications").forGetter(NotificationNode::getNotificationHolder),
            Codec.BOOL.fieldOf("sendToChat").forGetter(NotificationNode::sendsNotificationsToChat),
            Codec.BOOL.fieldOf("sendToMembers").forGetter(NotificationNode::sendsNotificationsToMembers),
            MemberLevel.CODEC.fieldOf("memberLevel").forGetter(NotificationNode::getNotificationMemberLevel)
    ).apply(builder, NotificationNode::new));

    public static final TraderNodeType<NotificationNode> TYPE = TraderNodeType.simple(NotificationNode::new,MAP_CODEC);

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_NOTIFY_MEMBERS = TextEntry.traderNodeValue(TYPE,"notify_members");
    public static final TextEntry VALUE_MEMBER_LEVEL = TextEntry.traderNodeValue(TYPE,"member_level");
    public static final TextEntry VALUE_PUSH_TO_CHAT = TextEntry.traderNodeValue(TYPE,"push_to_chat");

    public static final TextEntry TOOLTIP_TRADER_LOGS = TextEntry.tooltip(LCApi.MODID,"trader.log");
    public static final TextEntry TOOLTIP_TRADER_LOGS_SETTINGS = TextEntry.tooltip(LCApi.MODID,"trader.log.settings");

    public static final TextEntry GUI_MEMBER_LEVEL = TextEntry.gui(LCApi.MODID,"trader.log.member_level");

    private final NotificationHolder notifications;
    public NotificationHolder getNotificationHolder() { return this.notifications; }
    public List<NotificationStack> getNotifications() { return this.notifications.getNotifications(); }
    public List<NotificationStack> getNotifications(NotificationCategory category) { return this.notifications.getNotifications(category); }
    public List<NotificationStack> getNotifications(NotificationFilter filter) { return this.notifications.getNotifications(filter); }

    public static final NotificationFilter FILTER_SETTINGS = NotificationFilter.ofCategory(TraderSettingsCategory.INSTANCE);
    public static final NotificationFilter FILTER_NORMAL = FILTER_SETTINGS.inverted();

    private boolean pushToChat = false;
    @Override
    public boolean sendsNotificationsToChat() { return this.pushToChat; }
    public boolean setPushToChat(@Nullable PlayerReference admin,boolean pushToChat) {
        if(this.pushToChat != pushToChat) {
            this.pushToChat = pushToChat;
            this.setChanged(m -> m.setBoolean("pushToChat",this.pushToChat));
            if(admin != null)
                this.postInternalNotification(ChangeSettingNotification.simple(admin,VALUE_PUSH_TO_CHAT.get(),this.pushToChat));
            return true;
        }
        return false;
    }

    private boolean notifyMembers = false;
    @Override
    public boolean sendsNotificationsToMembers() { return this.notifyMembers; }
    public boolean setNotifyMembers(@Nullable PlayerReference admin,boolean notifyMembers) {
        if(this.notifyMembers != notifyMembers) {
            this.notifyMembers = notifyMembers;
            this.setChanged(m -> m.setBoolean("notifyMembers",this.notifyMembers));
            if(admin != null)
                this.postInternalNotification(ChangeSettingNotification.simple(admin,VALUE_NOTIFY_MEMBERS.get(),this.notifyMembers));
            return true;
        }
        return false;
    }

    private MemberLevel teamLevel = MemberLevel.MEMBERS;
    @Override
    public MemberLevel getNotificationMemberLevel() { return this.teamLevel; }
    public boolean setNotificationMemberLevel(@Nullable PlayerReference admin,MemberLevel level) {
        if(this.teamLevel != level) {
            this.teamLevel = level;
            this.setChanged(m -> m.setEnum("memberLevel",this.teamLevel));
            if(admin != null)
                this.postInternalNotification(ChangeSettingNotification.simple(admin,VALUE_MEMBER_LEVEL.get(),this.teamLevel.getBlurb()));
            return true;
        }
        return false;
    }
    public Component getMemberLevelBlurb() { return GUI_MEMBER_LEVEL.get(this.teamLevel.getBlurb()); }

    private NotificationNode() { this.notifications = new NotificationHolder().setSidedContext(this); }
    private NotificationNode(NotificationHolder notifications,boolean pushToChat,boolean sendToMembers, MemberLevel level) {
        this.notifications = notifications.setSidedContext(this);
        this.pushToChat = pushToChat;
        this.notifyMembers = sendToMembers;
        this.teamLevel = level;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public boolean isStorageOnly() { return true; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setList("allNotifications",LCFancyPacketTypes.NOTIFICATION_STACK,this.getNotifications())
                .setBoolean("pushToChat",this.pushToChat)
                .setBoolean("notifyMembers",this.notifyMembers)
                .setEnum("memberLevel",this.teamLevel);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("allNotifications"))
            this.notifications.loadFrom(data.getList("allNotifications",LCFancyPacketTypes.NOTIFICATION_STACK));
        if(data.contains("addNotification"))
            data.getList("addNotification",LCFancyPacketTypes.NOTIFICATION).forEach(this.notifications::addClientNotification);
        if(data.contains("pushToChat"))
            this.pushToChat = data.getBoolean("pushToChat");
        if(data.contains("notifyMembers"))
            this.notifyMembers = data.getBoolean("notifyMembers");
        if(data.contains("memberLevel"))
            this.teamLevel = data.getEnum("memberLevel",MemberLevel.class,this.teamLevel);
    }

    @Override
    public void pushNotification(Notification notification,boolean sendToMembers,MemberLevel targets,boolean pushToChat) {
        this.notifications.postNotification(notification);
        this.setChanged(builder -> builder.addToList("addNotification",LCFancyPacketTypes.NOTIFICATION,notification));
    }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {
        if(message.contains("deleteNotification") && this.isServer()) {
            if(this.getPermission(player,BuiltInPermissions.VIEW_LOGS).hasHigherPermission()) {
                FancyPacketMap entry = message.getMap("deleteNotification");
                int index = entry.getInt("index");
                NotificationFilter filter = entry.getBoolean("settingsView") ? FILTER_SETTINGS : FILTER_NORMAL;
                int trueIndex = this.notifications.deleteNotification(filter,index);
                //Resend all notifications
                //Sadly we can't optimize this to a "delete notification" packet as otherwise it'll delete it twice on the client
                if(trueIndex >= 0)
                    this.setChanged(m -> m.setList("allNotifications",LCFancyPacketTypes.NOTIFICATION_STACK,this.getNotifications()));
            }
        }
        if(message.contains("pushToChat") && this.getPermission(player,BuiltInPermissions.EDIT_SETTINGS))
            this.setPushToChat(PlayerReference.of(player),message.getBoolean("pushToChat"));
        if(message.contains("pushToMembers") && this.getPermission(player,BuiltInPermissions.EDIT_SETTINGS))
            this.setNotifyMembers(PlayerReference.of(player),message.getBoolean("pushToMembers"));
        if(message.contains("memberLevel") && this.getPermission(player,BuiltInPermissions.EDIT_SETTINGS))
            this.setNotificationMemberLevel(PlayerReference.of(player),message.getEnum("memberLevel",MemberLevel.class));
    }

    @Override
    public void addTabs(StorageTabBuilder builder) { builder.addTab(InfoTab::new); }

}

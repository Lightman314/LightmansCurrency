package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TraderInfoTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.INotificationConsumer;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ISidedListener;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.LoggerSettings;

import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LoggerNode extends SyncedTraderNode implements ISidedListener {

    private static final MapCodec<LoggerNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NotificationData.CODEC.fieldOf("logger").forGetter(n -> n.logger),
            Codec.BOOL.fieldOf("notificationsEnabled").forGetter(LoggerNode::getNotificationsEnabled),
            Codec.INT.fieldOf("teamNotificationLevel").forGetter(LoggerNode::getTeamNotificationLevel),
            Codec.BOOL.fieldOf("notificationsToChat").forGetter(LoggerNode::getNotificationsToChat),
            StatTracker.CODEC.fieldOf("stats").forGetter(n -> n.statTracker.getStatMap())
    ).apply(builder,LoggerNode::new));

    public static final TraderNodeType<LoggerNode> TYPE = TraderNodeType.simple(LoggerNode::new,MAP_CODEC);

    private final NotificationData logger;
    public List<Notification> getNotifications() { return this.logger.getNotifications(); }
    public List<Notification> getNotifications(NotificationCategory category) { return this.logger.getNotifications(category); }
    public List<Notification> getNotifications(Predicate<Notification> filter) { return this.logger.getNotifications(filter); }

    private boolean notificationsEnabled;
    public boolean getNotificationsEnabled() { return this.notificationsEnabled; }
    public boolean setNotificationsEnabled(@Nullable PlayerReference admin,boolean newValue)
    {
        if(this.notificationsEnabled != newValue)
        {
            this.notificationsEnabled = newValue;
            this.setChanged(builder -> builder.setBoolean("notificationsEnabled",newValue));
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_ENABLED.get(), this.notificationsEnabled));
            return true;
        }
        return false;
    }
    private int teamNotificationLevel = 0;
    public int getTeamNotificationLevel() { return this.teamNotificationLevel; }
    public boolean setTeamNotificationLevel(@Nullable PlayerReference admin, int newValue)
    {
        if(this.teamNotificationLevel != newValue)
        {
            this.teamNotificationLevel = newValue;
            this.setChanged(builder -> builder.setInt("teamNotificationLevel",newValue));
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_TEAM_NOTIFICATION_LEVEL.get(), this.teamNotificationLevel));
            return true;
        }
        return false;
    }
    private boolean notificationsToChat = false;
    public boolean getNotificationsToChat() { return this.notificationsToChat; }
    public boolean setNotificationsToChat(@Nullable PlayerReference admin, boolean newValue)
    {
        if(this.notificationsToChat != newValue)
        {
            this.notificationsToChat = newValue;
            this.setChanged(builder -> builder.setBoolean("notificationsToChat",newValue));
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_NOTIFICATIONS_TO_CHAT.get(), this.notificationsToChat));
            return true;
        }
        return false;
    }

    public final StatTracker statTracker = new StatTracker(this::onStatsChanged,this);

    private LoggerNode() { this.logger = new NotificationData(); }
    private LoggerNode(NotificationData logger, boolean notificationsEnabled, int teamNotificationLevel, boolean notificationsToChat, Map<String,StatType.Instance<?,?>> statData)
    {
        this.logger = logger;
        this.notificationsEnabled = notificationsEnabled;
        this.teamNotificationLevel = teamNotificationLevel;
        this.notificationsToChat = notificationsToChat;
        this.statTracker.load(statData);
    }

    public void addLog(Notification notification)
    {
        this.logger.addNotification(notification);
        this.setChanged(builder -> builder.addToList("addLog",notification,ModLazyPackets.NOTIFICATION));
    }

    public void postLogs(Supplier<Notification> source)
    {
        this.addLog(source.get());

        //Don't send logs to any other consumer if they're disabled
        if(!this.notificationsEnabled)
            return;

        for(TraderNode node : this.trader.getNodeIterable())
        {
            if(node instanceof INotificationConsumer consumer)
                consumer.pushNotification(source,this.teamNotificationLevel,this.notificationsToChat);
        }
    }

    private void onStatsChanged()
    {
        this.setChanged(builder -> builder.setCustom("stats",this.statTracker.getStatMap(), ModLazyPackets.STAT_TRACKER));
    }

    @Override
    public void onClientFlagSet() { this.logger.flagAsClient(this.trader); }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public boolean isStorageOnly() { return true; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setCustom("notifications",this.logger,ModLazyPackets.NOTIFICATION_DATA)
                .setBoolean("notificationsEnabled",this.notificationsEnabled)
                .setInt("teamNotificationLevel",this.teamNotificationLevel)
                .setBoolean("notificationsToChat",this.notificationsToChat)
                .setCustom("stats",this.statTracker.getStatMap(),ModLazyPackets.STAT_TRACKER);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("notifications"))
            this.logger.copyFrom(data.getCustom("notifications",ModLazyPackets.NOTIFICATION_DATA));
        if(data.contains("addLog"))
        {
            for(Notification n : data.getList("addLog",ModLazyPackets.NOTIFICATION))
                this.logger.addNotification(n);
        }
        if(data.contains("notificationsEnabled"))
            this.notificationsEnabled = data.getBoolean("notificationsEnabled");
        if(data.contains("teamNotificationLevel"))
            this.teamNotificationLevel = data.getInt("teamNotificationLevel");
        if(data.contains("notificationsToChat"))
            this.notificationsToChat = data.getBoolean("notificationsToChat");
        if(data.contains("stats"))
            this.statTracker.load(data.getCustom("stats",ModLazyPackets.STAT_TRACKER));
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Logger"))
            this.logger.copyFrom(DataContext.createNBT(lookup).read(tag.get("Logger"),NotificationData.CODEC));
        if(tag.contains("NotificationsEnabled"))
            this.notificationsEnabled = tag.getBoolean("NotificationsEnabled");
        if(tag.contains("ChatNotifications"))
            this.notificationsToChat = tag.getBoolean("ChatNotifications");
        if(tag.contains("TeamNotifications"))
            this.teamNotificationLevel = tag.getInt("TeamNotifications");
        if(tag.contains("Stats"))
            this.statTracker.load(tag.getCompound("Stats"),DataContext.createNBT(lookup));
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("Notifications"))
        {
            if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
            {
                boolean enable = message.getBoolean("Notifications");
                this.setNotificationsEnabled(PlayerReference.of(player),enable);
            }
        }
        if(message.contains("NotificationsToChat"))
        {
            if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
            {
                boolean enable = message.getBoolean("NotificationsToChat");
                this.setNotificationsToChat(PlayerReference.of(player),enable);
            }
        }
        if(message.contains("TeamNotificationLevel"))
        {
            if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
            {
                int level = message.getInt("TeamNotificationLevel");
                this.setTeamNotificationLevel(PlayerReference.of(player),level);
            }
        }
        if(message.contains("DeleteNotification"))
        {
            if(this.hasPermission(player,Permissions.TRANSFER_OWNERSHIP))
            {
                int index = message.getInt("DeleteNotification");
                Predicate<Notification> filter = message.getBoolean("SettingsView") ? TraderData.LOGS_SETTINGS_FILTER : TraderData.LOGS_NORMAL_FILTER;
                this.logger.deleteNotification(filter,index);
                this.setChanged(builder -> builder.setCustom("notifications",this.logger,ModLazyPackets.NOTIFICATION_DATA));
            }
        }
        if(message.contains("ClearStats"))
        {
            if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
            {
                boolean fullClear = message.getBoolean("ClearStats");
                this.statTracker.clear(fullClear);
            }
        }
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.VIEW_LOGS,1);
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new LoggerSettings(trader,this));
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new TraderInfoTab(menu));
    }

}

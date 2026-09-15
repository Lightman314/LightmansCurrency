package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public final class NotificationStack implements ISidedContext.Mutable<NotificationStack> {

    public static final Codec<NotificationStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Notification.CODEC.fieldOf("notification").forGetter(NotificationStack::getNotification),
            Codec.BOOL.fieldOf("seen").forGetter(NotificationStack::wasSeen),
            Codec.intRange(1,Integer.MAX_VALUE).fieldOf("count").forGetter(NotificationStack::getCount),
            Codec.LONG.fieldOf("timestamp").forGetter(NotificationStack::getTimestamp)
    ).apply(instance,NotificationStack::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationStack> STREAM_CODEC = StreamCodec.composite(
            Notification.STREAM_CODEC,NotificationStack::getNotification,
            ByteBufCodecs.BOOL,NotificationStack::wasSeen,
            ByteBufCodecs.VAR_INT,NotificationStack::getCount,
            ByteBufCodecs.LONG,NotificationStack::getTimestamp,
            NotificationStack::new);

    @Override
    public NotificationStack setSidedContext(ISidedContext context) { this.notification.setSidedContext(context); return this; }
    @Override
    public boolean isClient() { return this.notification.isClient(); }

    private final Notification notification;
    public Notification getNotification() { return this.notification; }
    public NotificationCategory getCategory() { return this.notification.getCategory(); }
    private boolean seen = false;
    public boolean wasSeen() { return this.seen; }
    public boolean isUnseen() { return !this.seen; }
    public void setSeen() { this.seen = true; }

    private int count = 1;
    public int getCount() { return this.count; }

    private long timestamp;
    public long getTimestamp() { return this.timestamp; }

    public NotificationStack(Notification notification) { this(notification,TimeHelper.getCurrentTime()); }
    public NotificationStack(Notification notification,long timestamp) { this.notification = notification; this.timestamp = Math.max(0,timestamp); }
    private NotificationStack(Notification notification,boolean seen,int count,long timestamp) {
        this.notification = notification;
        this.seen = seen;
        this.count = Math.max(1,count);
        this.timestamp = Math.max(0,timestamp);
    }

    public NotificationStack copy() { return new NotificationStack(this.notification,this.seen,this.count,this.timestamp); }
    public NotificationStack plainCopy() { return new NotificationStack(this.notification); }

    public List<Component> getMessageLines() { return this.notification.getMessageLines(); }
    public List<Component> getGeneralMessage() { return this.notification.getGeneralMessage(); }
    public List<Component> getChatMessage() { return this.notification.getChatMessage(); }

    public Component getTimeStampMessage() { return this.notification.getTimeStampMessage(this.timestamp); }

    public boolean tryMergeNotification(Notification other) {
        if(this.notification.canMerge(other)) {
            this.count++;
            this.seen = false;
            this.timestamp = TimeHelper.getCurrentTime();
            return true;
        }
        return false;
    }

    public List<Component> getModifiedMessage(UnaryOperator<Component> edit) {
        List<Component> message = new ArrayList<>(this.getMessageLines());
        if(message.isEmpty())
            return message;
        message.set(0,edit.apply(message.getFirst()));
        return message;
    }

}

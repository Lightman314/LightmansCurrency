package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketChatNotification extends ServerToClientPacket {

    public static final Type<SPacketChatNotification> TYPE = sType("chat_notification");
    public static final StreamCodec<RegistryFriendlyByteBuf,SPacketChatNotification> STREAM_CODEC = Notification.STREAM_CODEC.map(SPacketChatNotification::new,p -> p.notification);

    private final Notification notification;
    public SPacketChatNotification(Notification notification) {
        super(TYPE);
        this.notification = notification;
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(LCConfig.CLIENT.pushNotificationsToChat.get()) {
            for(Component line : this.notification.getChatMessage())
                player.sendSystemMessage(line);
        }
    }

}
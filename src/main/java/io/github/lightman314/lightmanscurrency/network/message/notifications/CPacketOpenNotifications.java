package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.common.menus.NotificationMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketOpenNotifications extends ClientToServerPacket.Simple {

    private static final CPacketOpenNotifications INSTANCE = new CPacketOpenNotifications();
    public static final Handler<CPacketOpenNotifications> HANDLER = new H();

    private CPacketOpenNotifications() {}

    public static void sendToServer() { INSTANCE.send(); }

    private static class H extends SimpleHandler<CPacketOpenNotifications>
    {
        private H() { super(INSTANCE); }
        @Override
        protected void handle(@Nonnull CPacketOpenNotifications message, @Nullable ServerPlayer sender) {
            NetworkHooks.openScreen(sender, NotificationMenu.PROVIDER);
        }
    }

}

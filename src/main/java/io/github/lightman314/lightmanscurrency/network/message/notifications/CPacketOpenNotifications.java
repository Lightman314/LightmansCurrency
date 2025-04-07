package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.common.menus.NotificationMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenNotifications extends ClientToServerPacket {

    private static final Type<CPacketOpenNotifications> TYPE = new Type<>(VersionUtil.lcResource("c_open_notifications"));
    private static final CPacketOpenNotifications INSTANCE = new CPacketOpenNotifications();
    public static final Handler<CPacketOpenNotifications> HANDLER = new H();

    private CPacketOpenNotifications() { super(TYPE); }

    public static void sendToServer() { INSTANCE.send(); }

    private static class H extends SimpleHandler<CPacketOpenNotifications>
    {
        private H() { super(TYPE, INSTANCE); }
        @Override
        protected void handle(@Nonnull CPacketOpenNotifications message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            player.openMenu(NotificationMenu.PROVIDER);
        }
    }

}

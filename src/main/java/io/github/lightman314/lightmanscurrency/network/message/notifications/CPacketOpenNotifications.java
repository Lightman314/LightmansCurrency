package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.common.menus.NotificationMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenNotifications extends ClientToServerPacket {

    private static final Type<CPacketOpenNotifications> TYPE = cType("open_notifications");
    private static final CPacketOpenNotifications INSTANCE = new CPacketOpenNotifications();
    public static final Handler<CPacketOpenNotifications> HANDLER = new H();

    private CPacketOpenNotifications() { super(TYPE); }

    public static void send() { INSTANCE.sendToServer(); }

    private static class H extends SimpleHandler<CPacketOpenNotifications>
    {
        private H() { super(TYPE, INSTANCE); }
        @Override
        protected void handle(CPacketOpenNotifications message, IPayloadContext context, Player player) {
            player.openMenu(NotificationMenu.PROVIDER);
        }
    }

}

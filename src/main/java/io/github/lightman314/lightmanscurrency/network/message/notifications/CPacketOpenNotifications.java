package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.common.menus.NotificationMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

public class CPacketOpenNotifications extends ClientToServerPacket.Simple {

    private static final CPacketOpenNotifications INSTANCE = new CPacketOpenNotifications();
    public static final Handler<CPacketOpenNotifications> HANDLER = new H();

    private CPacketOpenNotifications() {}

    public static void sendToServer() { INSTANCE.send(); }

    private static class H extends SimpleHandler<CPacketOpenNotifications>
    {
        private H() { super(INSTANCE); }
        @Override
        protected void handle(CPacketOpenNotifications message, Player player) {
            if(player instanceof ServerPlayer sp)
                NetworkHooks.openScreen(sp, NotificationMenu.PROVIDER);
        }
    }

}

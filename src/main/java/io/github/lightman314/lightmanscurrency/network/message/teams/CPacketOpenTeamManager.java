package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenTeamManager extends ClientToServerPacket.Simple {

    private static final CPacketOpenTeamManager INSTANCE = new CPacketOpenTeamManager();
    public static final Handler<CPacketOpenTeamManager> HANDLER = new H();

    private CPacketOpenTeamManager() {}

    public static void sendToServer() { INSTANCE.send(); }

    private static class H extends SimpleHandler<CPacketOpenTeamManager>
    {

        protected H() { super(INSTANCE); }

        @Override
        protected void handle(CPacketOpenTeamManager message, Player player) {
            player.openMenu(TeamManagementMenu.PROVIDER);
        }
    }

}

package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenTeamManager extends ClientToServerPacket {

	private static final Type<CPacketOpenTeamManager> TYPE = cType("open_team_manager");
	private static final CPacketOpenTeamManager INSTANCE = new CPacketOpenTeamManager();
	public static final Handler<CPacketOpenTeamManager> HANDLER = new H();

	private CPacketOpenTeamManager()  { super(TYPE); }

	public static void send() { INSTANCE.sendToServer(); }

	private static class H extends SimpleHandler<CPacketOpenTeamManager>
	{
		protected H() { super(TYPE, INSTANCE); }

		@Override
		protected void handle(CPacketOpenTeamManager message, IPayloadContext context, Player player) {
			player.openMenu(TeamManagementMenu.PROVIDER);
		}

	}

}

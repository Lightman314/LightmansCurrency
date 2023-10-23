package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketCreateTeam extends ClientToServerPacket {

	public static final Handler<CPacketCreateTeam> HANDLER = new H();

	String teamName;

	public CPacketCreateTeam(String teamName)  { this.teamName = teamName; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeUtf(this.teamName, Team.MAX_NAME_LENGTH); }

	private static class H extends Handler<CPacketCreateTeam>
	{
		@Nonnull
		@Override
		public CPacketCreateTeam decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreateTeam(buffer.readUtf(Team.MAX_NAME_LENGTH)); }
		@Override
		protected void handle(@Nonnull CPacketCreateTeam message, @Nullable ServerPlayer sender) {
			if(sender == null)
				return;
			Team newTeam = TeamSaveData.RegisterTeam(sender, message.teamName);
			if(newTeam != null)
				new SPacketCreateTeamResponse(newTeam.getID()).sendTo(sender);
		}
	}

}

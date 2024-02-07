package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketEditTeam extends ClientToServerPacket {

	public static final Handler<CPacketEditTeam> HANDLER = new H();

	long teamID;
	LazyPacketData request;
	
	public CPacketEditTeam(long teamID, LazyPacketData request)
	{
		this.teamID = teamID;
		this.request = request;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.teamID);
		this.request.encode(buffer);
	}

	private static class H extends Handler<CPacketEditTeam>
	{
		@Nonnull
		@Override
		public CPacketEditTeam decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketEditTeam(buffer.readLong(), LazyPacketData.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketEditTeam message, @Nullable ServerPlayer sender) {
			Team team = TeamSaveData.GetTeam(false, message.teamID);
			if(sender != null && team != null)
				team.HandleEditRequest(sender, message.request);
		}
	}

}

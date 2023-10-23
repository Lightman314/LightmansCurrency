package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketCreateTeamResponse extends ServerToClientPacket {

	public static final Handler<SPacketCreateTeamResponse> HANDLER = new H();

	long teamID;
	
	public SPacketCreateTeamResponse(long teamID) { this.teamID = teamID; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.teamID); }

	private static class H extends Handler<SPacketCreateTeamResponse>
	{
		@Nonnull
		@Override
		public SPacketCreateTeamResponse decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketCreateTeamResponse(buffer.readLong()); }
		@Override
		protected void handle(@Nonnull SPacketCreateTeamResponse message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.createTeamResponse(message.teamID);
		}
	}

}

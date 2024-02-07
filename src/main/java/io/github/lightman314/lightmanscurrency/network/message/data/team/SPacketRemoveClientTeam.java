package io.github.lightman314.lightmanscurrency.network.message.data.team;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketRemoveClientTeam extends ServerToClientPacket {

	public static final Handler<SPacketRemoveClientTeam> HANDLER = new H();

	long teamID;
	
	public SPacketRemoveClientTeam(long teamID) { this.teamID = teamID; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.teamID); }

	private static class H extends Handler<SPacketRemoveClientTeam>
	{
		@Nonnull
		@Override
		public SPacketRemoveClientTeam decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketRemoveClientTeam(buffer.readLong()); }
		@Override
		protected void handle(@Nonnull SPacketRemoveClientTeam message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.removeTeam(message.teamID);
		}
	}

}

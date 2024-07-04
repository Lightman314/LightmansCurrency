package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketCreateTeamResponse extends ServerToClientPacket {

	private static final Type<SPacketCreateTeamResponse> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_team_create_reply"));
	public static final Handler<SPacketCreateTeamResponse> HANDLER = new H();

	long teamID;
	
	public SPacketCreateTeamResponse(long teamID) { super(TYPE); this.teamID = teamID; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketCreateTeamResponse message) { buffer.writeLong(message.teamID); }
	private static SPacketCreateTeamResponse decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketCreateTeamResponse(buffer.readLong()); }

	private static class H extends Handler<SPacketCreateTeamResponse>
	{
		protected H() { super(TYPE, easyCodec(SPacketCreateTeamResponse::encode,SPacketCreateTeamResponse::decode)); }
		@Override
		protected void handle(@Nonnull SPacketCreateTeamResponse message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.PROXY.createTeamResponse(message.teamID);
		}
	}

}

package io.github.lightman314.lightmanscurrency.network.message.data.team;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketRemoveClientTeam extends ServerToClientPacket {

	private static final Type<SPacketRemoveClientTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_team_delete_client"));
	public static final Handler<SPacketRemoveClientTeam> HANDLER = new H();

	long teamID;
	
	public SPacketRemoveClientTeam(long teamID) { super(TYPE); this.teamID = teamID; }
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketRemoveClientTeam message) { buffer.writeLong(message.teamID); }
	private static SPacketRemoveClientTeam decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketRemoveClientTeam(buffer.readLong()); }

	private static class H extends Handler<SPacketRemoveClientTeam>
	{
		protected H() { super(TYPE, easyCodec(SPacketRemoveClientTeam::encode,SPacketRemoveClientTeam::decode)); }
		@Override
		protected void handle(@Nonnull SPacketRemoveClientTeam message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().removeTeam(message.teamID);
		}
	}

}

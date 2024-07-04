package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketEditTeam extends ClientToServerPacket {

	private static final Type<CPacketEditTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_team_edit"));
	public static final Handler<CPacketEditTeam> HANDLER = new H();

	long teamID;
	LazyPacketData request;
	
	public CPacketEditTeam(long teamID, LazyPacketData request)
	{
		super(TYPE);
		this.teamID = teamID;
		this.request = request;
	}

	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull CPacketEditTeam message)
	{
		buffer.writeLong(message.teamID);
		message.request.encode(buffer);
	}
	private static CPacketEditTeam decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new CPacketEditTeam(buffer.readLong(),LazyPacketData.decode(buffer)); }

	private static class H extends Handler<CPacketEditTeam>
	{
		protected H() { super(TYPE, fancyCodec(CPacketEditTeam::encode,CPacketEditTeam::decode)); }
		@Override
		protected void handle(@Nonnull CPacketEditTeam message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			Team team = TeamSaveData.GetTeam(false, message.teamID);
			if(team != null)
				team.HandleEditRequest(player, message.request);
		}
	}

}

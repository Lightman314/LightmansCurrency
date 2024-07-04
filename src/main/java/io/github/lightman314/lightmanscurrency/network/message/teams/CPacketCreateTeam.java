package io.github.lightman314.lightmanscurrency.network.message.teams;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketCreateTeam extends ClientToServerPacket {

	private static final Type<CPacketCreateTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_team_create"));
	public static final Handler<CPacketCreateTeam> HANDLER = new H();

	String teamName;

	public CPacketCreateTeam(String teamName)  { super(TYPE); this.teamName = teamName; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketCreateTeam message) { buffer.writeUtf(message.teamName, Team.MAX_NAME_LENGTH); }
	private static CPacketCreateTeam decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreateTeam(buffer.readUtf(Team.MAX_NAME_LENGTH)); }

	private static class H extends Handler<CPacketCreateTeam>
	{
		protected H() { super(TYPE, easyCodec(CPacketCreateTeam::encode,CPacketCreateTeam::decode)); }

		@Override
		protected void handle(@Nonnull CPacketCreateTeam message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			Team newTeam = TeamSaveData.RegisterTeam(player, message.teamName);
			if(newTeam != null)
				context.reply(new SPacketCreateTeamResponse(newTeam.getID()));
		}
	}

}

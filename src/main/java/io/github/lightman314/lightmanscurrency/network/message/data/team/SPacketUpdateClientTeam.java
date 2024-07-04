package io.github.lightman314.lightmanscurrency.network.message.data.team;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketUpdateClientTeam extends ServerToClientPacket {

	private static final Type<SPacketUpdateClientTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_team_update_client"));
	public static final Handler<SPacketUpdateClientTeam> HANDLER = new H();

	CompoundTag teamData;
	
	public SPacketUpdateClientTeam(CompoundTag teamData) { super(TYPE); this.teamData = teamData; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketUpdateClientTeam message) { buffer.writeNbt(message.teamData); }
	private static SPacketUpdateClientTeam decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientTeam(readNBT(buffer)); }

	private static class H extends Handler<SPacketUpdateClientTeam>
	{
		protected H() { super(TYPE, easyCodec(SPacketUpdateClientTeam::encode,SPacketUpdateClientTeam::decode)); }
		@Override
		protected void handle(@Nonnull SPacketUpdateClientTeam message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.PROXY.updateTeam(message.teamData);
		}
	}

}

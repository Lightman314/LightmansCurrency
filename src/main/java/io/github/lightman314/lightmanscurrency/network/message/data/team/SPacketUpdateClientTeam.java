package io.github.lightman314.lightmanscurrency.network.message.data.team;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketUpdateClientTeam extends ServerToClientPacket {

	public static final Handler<SPacketUpdateClientTeam> HANDLER = new H();

	CompoundTag traderData;
	
	public SPacketUpdateClientTeam(CompoundTag traderData) {
		this.traderData = traderData;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.traderData); }

	private static class H extends Handler<SPacketUpdateClientTeam>
	{
		@Nonnull
		@Override
		public SPacketUpdateClientTeam decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientTeam(buffer.readAnySizeNbt()); }
		@Override
		protected void handle(@Nonnull SPacketUpdateClientTeam message, @Nullable ServerPlayer sender) {
			LightmansCurrency.getProxy().updateTeam(message.traderData);
		}
	}

}

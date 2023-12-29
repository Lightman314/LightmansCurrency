package io.github.lightman314.lightmanscurrency.network.message.data.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketUpdateClientTrader extends ServerToClientPacket {

	public static final Handler<SPacketUpdateClientTrader> HANDLER = new H();

	CompoundTag traderData;
	
	public SPacketUpdateClientTrader(CompoundTag traderData) { this.traderData = traderData; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.traderData); }

	private static class H extends Handler<SPacketUpdateClientTrader>
	{
		@Nonnull
		@Override
		public SPacketUpdateClientTrader decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientTrader(buffer.readAnySizeNbt()); }
		@Override
		protected void handle(@Nonnull SPacketUpdateClientTrader message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.updateTrader(message.traderData);
		}
	}

}

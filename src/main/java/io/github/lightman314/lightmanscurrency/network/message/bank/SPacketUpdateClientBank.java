package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SPacketUpdateClientBank extends ServerToClientPacket {

	public static final Handler<SPacketUpdateClientBank> HANDLER = new H();

	CompoundTag traderData;
	
	public SPacketUpdateClientBank(CompoundTag traderData) { this.traderData = traderData; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.traderData); }

	private static class H extends Handler<SPacketUpdateClientBank>
	{
		@Nonnull
		@Override
		public SPacketUpdateClientBank decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientBank(buffer.readNbt()); }
		@Override
		protected void handle(@Nonnull SPacketUpdateClientBank message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.updateBankAccount(message.traderData);
		}
	}

}
